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

package io.hermes.discovery.zen.ping.unicast;

import static com.google.common.collect.Lists.newArrayList;
import static io.hermes.discovery.zen.ping.ZenPing.PingResponse.readPingResponse;
import static io.hermes.util.TimeValue.readTimeValue;
import static io.hermes.util.concurrent.ConcurrentMaps.newConcurrentMap;

import com.google.common.collect.ImmutableList;
import io.hermes.HermesException;
import io.hermes.HermesIllegalArgumentException;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.discovery.zen.DiscoveryNodesProvider;
import io.hermes.discovery.zen.ping.ZenPing;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.BaseTransportRequestHandler;
import io.hermes.transport.BaseTransportResponseHandler;
import io.hermes.transport.ConnectTransportException;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.TransportChannel;
import io.hermes.transport.TransportService;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.settings.Settings;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author spancer.ray
 */
public class UnicastZenPing extends AbstractLifecycleComponent<ZenPing> implements ZenPing {

  private final ThreadPool threadPool;

  private final TransportService transportService;

  private final ClusterName clusterName;


  private final String[] hosts;

  private final DiscoveryNode[] nodes;
  private final AtomicInteger pingIdGenerator = new AtomicInteger();
  private final Map<Integer, ConcurrentMap<DiscoveryNode, PingResponse>> receivedResponses =
      newConcurrentMap();
  // a list of temporal responses a node will return for a request (holds requests from other nodes)
  private final Queue<PingResponse> temporalResponses = new LinkedTransferQueue<PingResponse>();
  private volatile DiscoveryNodesProvider nodesProvider;

  public UnicastZenPing(Settings settings, ThreadPool threadPool, TransportService transportService,
      ClusterName clusterName) {
    super(settings);
    this.threadPool = threadPool;
    this.transportService = transportService;
    this.clusterName = clusterName;

    this.hosts = componentSettings.getAsArray("hosts", new String[]{"127.0.0.1:9400"});
    this.nodes = new DiscoveryNode[hosts.length];
    for (int i = 0; i < hosts.length; i++) {
      try {
        nodes[i] = new DiscoveryNode("#zen_unicast_" + i + "#",
            transportService.addressFromString(hosts[i]));
      } catch (Exception e) {
        throw new HermesIllegalArgumentException("Failed to resolve address for [" + hosts[i] + "]",
            e);
      }
    }

    transportService.registerHandler(UnicastPingRequestHandler.ACTION,
        new UnicastPingRequestHandler());
  }

  @Override
  protected void doStart() throws HermesException {
  }

  @Override
  protected void doStop() throws HermesException {
  }

  @Override
  protected void doClose() throws HermesException {
    transportService.removeHandler(UnicastPingRequestHandler.ACTION);
  }

  protected List<DiscoveryNode> buildDynamicNodes() {
    return ImmutableList.of();
  }

  @Override
  public void setNodesProvider(DiscoveryNodesProvider nodesProvider) {
    this.nodesProvider = nodesProvider;
  }

  @Override
  public void ping(final PingListener listener, final TimeValue timeout) throws HermesException {
    final int id = pingIdGenerator.incrementAndGet();
    receivedResponses.put(id, new ConcurrentHashMap<DiscoveryNode, PingResponse>());
    sendPings(id, timeout, false);
    threadPool.schedule(new Runnable() {
      @Override
      public void run() {
        sendPings(id, timeout, true);
        ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses.remove(id);
        listener.onPing(responses.values().toArray(new PingResponse[responses.size()]));
      }
    }, timeout);
  }

  private void sendPings(int id, TimeValue timeout, boolean wait) {
    UnicastPingRequest pingRequest = new UnicastPingRequest();
    pingRequest.id = id;
    pingRequest.timeout = timeout;
    DiscoveryNodes discoNodes = nodesProvider.nodes();

    pingRequest.pingResponse =
        new PingResponse(discoNodes.localNode(), discoNodes.masterNode(), clusterName);

    List<DiscoveryNode> nodesToPing = newArrayList(nodes);
    nodesToPing.addAll(buildDynamicNodes());

    final CountDownLatch latch = new CountDownLatch(nodesToPing.size());
    for (final DiscoveryNode node : nodesToPing) {
      // make sure we are connected
      boolean disconnectX;
      DiscoveryNode nodeToSendX = discoNodes.findByAddress(node.address());
      if (nodeToSendX != null) {
        disconnectX = false;
      } else {
        nodeToSendX = node;
        disconnectX = true;
      }
      final DiscoveryNode nodeToSend = nodeToSendX;
      try {
        transportService.connectToNode(nodeToSend);
      } catch (ConnectTransportException e) {
        latch.countDown();
        // can't connect to the node
        continue;
      }

      final boolean disconnect = disconnectX;
      transportService.sendRequest(nodeToSend, UnicastPingRequestHandler.ACTION, pingRequest,
          TimeValue.timeValueMillis((long) (timeout.millis() * 1.25)),
          new BaseTransportResponseHandler<UnicastPingResponse>() {

            @Override
            public UnicastPingResponse newInstance() {
              return new UnicastPingResponse();
            }

            @Override
            public void handleResponse(UnicastPingResponse response) {
              try {
                DiscoveryNodes discoveryNodes = nodesProvider.nodes();
                for (PingResponse pingResponse : response.pingResponses) {
                  if (disconnect) {
                    transportService.disconnectFromNode(nodeToSend);
                  }
                  if (pingResponse.target().id().equals(discoveryNodes.localNodeId())) {
                    // that's us, ignore
                    continue;
                  }
                  if (!pingResponse.clusterName().equals(clusterName)) {
                    // not part of the cluster
                    return;
                  }
                  ConcurrentMap<DiscoveryNode, PingResponse> responses =
                      receivedResponses.get(response.id);
                  if (responses == null) {
                    logger.warn("Received ping response with no matching id [{}]", response.id);
                  } else {
                    responses.put(pingResponse.target(), pingResponse);
                  }
                }
              } finally {
                latch.countDown();
              }
            }

            @Override
            public void handleException(RemoteTransportException exp) {
              latch.countDown();
              if (exp instanceof ConnectTransportException) {
                // ok, not connected...
              } else {
                if (disconnect) {
                  transportService.disconnectFromNode(nodeToSend);
                }
                logger.warn("Failed to send ping to [{}]", exp, node);
              }
            }
          });
    }
    if (wait) {
      try {
        latch.await(timeout.millis() * 5, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  private UnicastPingResponse handlePingRequest(final UnicastPingRequest request) {
    temporalResponses.add(request.pingResponse);
    threadPool.schedule(new Runnable() {
      @Override
      public void run() {
        temporalResponses.remove(request.pingResponse);
      }
    }, request.timeout.millis() * 2, TimeUnit.MILLISECONDS);

    List<PingResponse> pingResponses = newArrayList(temporalResponses);
    DiscoveryNodes discoNodes = nodesProvider.nodes();
    pingResponses
        .add(new PingResponse(discoNodes.localNode(), discoNodes.masterNode(), clusterName));

    UnicastPingResponse unicastPingResponse = new UnicastPingResponse();
    unicastPingResponse.id = request.id;
    unicastPingResponse.pingResponses =
        pingResponses.toArray(new PingResponse[pingResponses.size()]);

    return unicastPingResponse;
  }

  static class UnicastPingRequest implements Streamable {

    int id;

    TimeValue timeout;

    PingResponse pingResponse;

    UnicastPingRequest() {
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      id = in.readInt();
      timeout = readTimeValue(in);
      pingResponse = readPingResponse(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      out.writeInt(id);
      timeout.writeTo(out);
      pingResponse.writeTo(out);
    }
  }

  static class UnicastPingResponse implements Streamable {

    int id;

    PingResponse[] pingResponses;

    UnicastPingResponse() {
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      id = in.readInt();
      pingResponses = new PingResponse[in.readVInt()];
      for (int i = 0; i < pingResponses.length; i++) {
        pingResponses[i] = readPingResponse(in);
      }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      out.writeInt(id);
      out.writeVInt(pingResponses.length);
      for (PingResponse pingResponse : pingResponses) {
        pingResponse.writeTo(out);
      }
    }
  }

  class UnicastPingRequestHandler extends BaseTransportRequestHandler<UnicastPingRequest> {

    static final String ACTION = "discovery/zen/unicast";

    @Override
    public UnicastPingRequest newInstance() {
      return new UnicastPingRequest();
    }

    @Override
    public void messageReceived(UnicastPingRequest request, TransportChannel channel)
        throws Exception {
      channel.sendResponse(handlePingRequest(request));
    }
  }
}
