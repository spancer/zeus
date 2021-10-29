/*******************************************************************************
 * Copyright 2021 spancer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package io.hermes.discovery.zen.fd;

import static io.hermes.cluster.node.DiscoveryNode.readNode;
import static io.hermes.util.TimeValue.timeValueSeconds;

import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.discovery.zen.DiscoveryNodesProvider;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.BaseTransportRequestHandler;
import io.hermes.transport.BaseTransportResponseHandler;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.TransportChannel;
import io.hermes.transport.TransportConnectionListener;
import io.hermes.transport.TransportService;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.settings.Settings;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author spancer.ray
 */
public class MasterFaultDetection extends AbstractComponent {

  private final ThreadPool threadPool;
  private final TransportService transportService;
  private final DiscoveryNodesProvider nodesProvider;
  private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
  private final boolean connectOnNetworkDisconnect;
  private final TimeValue pingInterval;
  private final TimeValue pingRetryTimeout;
  private final int pingRetryCount;
  private final FDConnectionListener connectionListener;
  private final AtomicBoolean notifiedMasterFailure = new AtomicBoolean();
  private volatile DiscoveryNode masterNode;

  private volatile int retryCount;

  public MasterFaultDetection(Settings settings, ThreadPool threadPool,
      TransportService transportService, DiscoveryNodesProvider nodesProvider) {
    super(settings);
    this.threadPool = threadPool;
    this.transportService = transportService;
    this.nodesProvider = nodesProvider;

    this.connectOnNetworkDisconnect =
        componentSettings.getAsBoolean("connect_on_network_disconnect", false);
    this.pingInterval = componentSettings.getAsTime("ping_interval", timeValueSeconds(1));
    this.pingRetryTimeout = componentSettings.getAsTime("ping_timeout", timeValueSeconds(6));
    this.pingRetryCount = componentSettings.getAsInt("ping_retries", 5);

    this.connectionListener = new FDConnectionListener();
    transportService.addConnectionListener(connectionListener);

    transportService.registerHandler(MasterPingRequestHandler.ACTION,
        new MasterPingRequestHandler());
  }

  public DiscoveryNode masterNode() {
    return this.masterNode;
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  public void restart(DiscoveryNode masterNode) {
    stop();
    start(masterNode);
  }

  public void start(DiscoveryNode masterNode) {
    this.masterNode = masterNode;
    this.retryCount = 0;
    this.notifiedMasterFailure.set(false);

    // try and connect to make sure we are connected
    try {
      transportService.connectToNode(masterNode);
    } catch (Exception e) {
      notifyMasterFailure(masterNode);
    }

    // start the ping process
    threadPool.schedule(new SendPingRequest(), pingInterval);
  }

  public void stop() {
    // also will stop the next ping schedule
    this.retryCount = 0;
    this.masterNode = null;
  }

  public void close() {
    stop();
    this.listeners.clear();
    transportService.removeConnectionListener(connectionListener);
    transportService.removeHandler(MasterPingRequestHandler.ACTION);
  }

  private void handleTransportDisconnect(DiscoveryNode node) {
    if (!node.equals(this.masterNode)) {
      return;
    }
    if (connectOnNetworkDisconnect) {
      try {
        transportService.connectToNode(node);
      } catch (Exception e) {
        logger.trace("Master [{}] failed on disconnect (with verified connect)", masterNode);
        notifyMasterFailure(masterNode);
      }
    } else {
      logger.trace("Master [{}] failed on disconnect", masterNode);
      notifyMasterFailure(masterNode);
    }
  }

  private void notifyDisconnectedFromMaster() {
    for (Listener listener : listeners) {
      listener.onDisconnectedFromMaster();
    }
    // we don't stop on disconnection from master, we keep pinging it
  }

  private void notifyMasterFailure(DiscoveryNode masterNode) {
    if (notifiedMasterFailure.compareAndSet(false, true)) {
      for (Listener listener : listeners) {
        listener.onMasterFailure(masterNode);
      }
      stop();
    }
  }

  public interface Listener {

    void onMasterFailure(DiscoveryNode masterNode);

    void onDisconnectedFromMaster();
  }

  private class FDConnectionListener implements TransportConnectionListener {

    @Override
    public void onNodeConnected(DiscoveryNode node) {
    }

    @Override
    public void onNodeDisconnected(DiscoveryNode node) {
      handleTransportDisconnect(node);
    }
  }

  private class SendPingRequest implements Runnable {

    @Override
    public void run() {
      if (masterNode != null) {
        final DiscoveryNode sentToNode = masterNode;
        transportService.sendRequest(masterNode, MasterPingRequestHandler.ACTION,
            new MasterPingRequest(nodesProvider.nodes().localNode()), pingRetryTimeout,
            new BaseTransportResponseHandler<MasterPingResponseResponse>() {
              @Override
              public MasterPingResponseResponse newInstance() {
                return new MasterPingResponseResponse();
              }

              @Override
              public void handleResponse(MasterPingResponseResponse response) {
                // check if the master node did not get switched on us...
                if (sentToNode.equals(MasterFaultDetection.this.masterNode())) {
                  if (!response.connectedToMaster) {
                    logger.trace("Master [{}] does not have us registered with it...", masterNode);
                    notifyDisconnectedFromMaster();
                  } else {
                    threadPool.schedule(SendPingRequest.this, pingInterval);
                  }
                }
              }

              @Override
              public void handleException(RemoteTransportException exp) {
                // check if the master node did not get switched on us...
                if (sentToNode.equals(MasterFaultDetection.this.masterNode())) {
                  int retryCount = ++MasterFaultDetection.this.retryCount;
                  logger.trace("Master [{}] failed to ping, retry [{}] out of [{}]", exp,
                      masterNode, retryCount, pingRetryCount);
                  if (retryCount >= pingRetryCount) {
                    logger.trace("Master [{}] failed on ping", masterNode);
                    // not good, failure
                    notifyMasterFailure(sentToNode);
                  }
                }
              }
            });
      }
    }
  }

  private class MasterPingRequestHandler extends BaseTransportRequestHandler<MasterPingRequest> {

    public static final String ACTION = "discovery/zen/fd/masterPing";

    @Override
    public MasterPingRequest newInstance() {
      return new MasterPingRequest();
    }

    @Override
    public void messageReceived(MasterPingRequest request, TransportChannel channel)
        throws Exception {
      DiscoveryNodes nodes = nodesProvider.nodes();
      channel.sendResponse(new MasterPingResponseResponse(nodes.nodeExists(request.node.id())));
    }
  }


  private class MasterPingRequest implements Streamable {

    private DiscoveryNode node;

    private MasterPingRequest() {
    }

    private MasterPingRequest(DiscoveryNode node) {
      this.node = node;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      node = readNode(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      node.writeTo(out);
    }
  }

  private class MasterPingResponseResponse implements Streamable {

    private boolean connectedToMaster;

    private MasterPingResponseResponse() {
    }

    private MasterPingResponseResponse(boolean connectedToMaster) {
      this.connectedToMaster = connectedToMaster;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      connectedToMaster = in.readBoolean();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      out.writeBoolean(connectedToMaster);
    }
  }
}
