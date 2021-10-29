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



package io.hermes.client.transport;


import static io.hermes.util.TimeValue.timeValueSeconds;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.action.TransportActions;
import io.hermes.action.admin.cluster.node.info.NodesInfoResponse;
import io.hermes.client.Requests;
import io.hermes.cluster.ClusterChangedEvent;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.ClusterStateListener;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.BaseTransportResponseHandler;
import io.hermes.transport.ConnectTransportException;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.TransportService;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.Settings;
import io.hermes.util.transport.TransportAddress;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author spancer.ray
 */
public class TransportClientNodesService extends AbstractComponent implements ClusterStateListener {

  private final TimeValue nodesSamplerInterval;

  private final ClusterName clusterName;

  private final TransportService transportService;

  private final ThreadPool threadPool;
  private final Object transportMutex = new Object();
  private final AtomicInteger tempNodeIdGenerator = new AtomicInteger();
  private final ScheduledNodesSampler nodesSampler = new ScheduledNodesSampler();
  private final ScheduledFuture nodesSamplerFuture;
  private final AtomicInteger randomNodeGenerator = new AtomicInteger();
  // nodes that are added to be discovered
  private volatile ImmutableList<DiscoveryNode> listedNodes = ImmutableList.of();
  private volatile ImmutableList<DiscoveryNode> nodes = ImmutableList.of();
  private volatile DiscoveryNodes discoveredNodes;

  @Inject
  public TransportClientNodesService(Settings settings, ClusterName clusterName,
      TransportService transportService, ThreadPool threadPool) {
    super(settings);
    this.clusterName = clusterName;
    this.transportService = transportService;
    this.threadPool = threadPool;

    this.nodesSamplerInterval =
        componentSettings.getAsTime("nodes_sampler_interval", timeValueSeconds(1));

    if (logger.isDebugEnabled()) {
      logger.debug("node_sampler_interval[" + nodesSamplerInterval + "]");
    }

    this.nodesSamplerFuture = threadPool.scheduleWithFixedDelay(nodesSampler, nodesSamplerInterval);

    // we want the transport service to throw connect exceptions, so we can retry
    transportService.throwConnectException(true);
  }

  public ImmutableList<TransportAddress> transportAddresses() {
    ImmutableList.Builder<TransportAddress> lstBuilder = ImmutableList.builder();
    for (DiscoveryNode listedNode : listedNodes) {
      lstBuilder.add(listedNode.address());
    }
    return lstBuilder.build();
  }

  public ImmutableList<DiscoveryNode> connectedNodes() {
    return this.nodes;
  }

  public TransportClientNodesService addTransportAddress(TransportAddress transportAddress) {
    synchronized (transportMutex) {
      ImmutableList.Builder<DiscoveryNode> builder = ImmutableList.builder();
      listedNodes = builder.addAll(listedNodes).add(
          new DiscoveryNode("#temp#-" + tempNodeIdGenerator.incrementAndGet(), transportAddress))
          .build();
    }
    nodesSampler.run();
    return this;
  }


  public TransportClientNodesService addTransportAddresses(TransportAddress... transportAddresses) {
    synchronized (transportMutex) {
      List<TransportAddress> filtered = Lists
          .newArrayListWithExpectedSize(transportAddresses.length);
      for (TransportAddress transportAddress : transportAddresses) {
        boolean found = false;
        for (DiscoveryNode otherNode : listedNodes) {
          if (otherNode.address().equals(transportAddress)) {
            found = true;
            logger.debug("address [{}] already exists with [{}], ignoring...", transportAddress,
                otherNode);
            break;
          }
        }
        if (!found) {
          filtered.add(transportAddress);
        }
      }
      if (filtered.isEmpty()) {
        return this;
      }
      ImmutableList.Builder<DiscoveryNode> builder = ImmutableList.builder();
      builder.addAll(listedNodes());
      for (TransportAddress transportAddress : filtered) {
        DiscoveryNode node = new DiscoveryNode(
            "#transport#-" + tempNodeIdGenerator.incrementAndGet(), transportAddress);
        logger.debug("adding address [{}]", node);
        builder.add(node);
      }
      listedNodes = builder.build();
    }

    /**
     * 需要增加内容
     */

//    nodesSampler.sample();
    return this;
  }

  public ImmutableList<DiscoveryNode> listedNodes() {
    return this.listedNodes;
  }

  public TransportClientNodesService removeTransportAddress(TransportAddress transportAddress) {
    synchronized (transportMutex) {
      ImmutableList.Builder<DiscoveryNode> builder = ImmutableList.builder();
      for (DiscoveryNode otherNode : listedNodes) {
        if (!otherNode.address().equals(transportAddress)) {
          builder.add(otherNode);
        }
      }
      listedNodes = builder.build();
    }
    nodesSampler.run();
    return this;
  }

  public <T> T execute(NodeCallback<T> callback) throws HermesException {
    ImmutableList<DiscoveryNode> nodes = this.nodes;
    if (nodes.isEmpty()) {
      throw new NoNodeAvailableException();
    }
    int index = randomNodeGenerator.incrementAndGet();
    for (int i = 0; i < nodes.size(); i++) {
      DiscoveryNode node = nodes.get((index + i) % nodes.size());
      try {
        return callback.doWithNode(node);
      } catch (ConnectTransportException e) {
        // retry in this case
      }
    }
    throw new NoNodeAvailableException();
  }

  public void close() {
    nodesSamplerFuture.cancel(true);
    for (DiscoveryNode listedNode : listedNodes) {
      transportService.disconnectFromNode(listedNode);
    }
  }

  @Override
  public void clusterChanged(ClusterChangedEvent event) {
    for (DiscoveryNode node : event.nodesDelta().addedNodes()) {
      try {
        transportService.connectToNode(node);
      } catch (Exception e) {
        logger.warn("Failed to connect to discovered node [" + node + "]", e);
      }
    }
    this.discoveredNodes = event.state().nodes();
    HashSet<DiscoveryNode> newNodes = new HashSet<DiscoveryNode>(nodes);
    newNodes.addAll(discoveredNodes.nodes().values());
    nodes = new ImmutableList.Builder<DiscoveryNode>().addAll(newNodes).build();

    logger.info("discoveryNode num: {} ", nodes.size());
    nodes.forEach(new Consumer<DiscoveryNode>() {
      @Override
      public void accept(DiscoveryNode discoveryNode) {
        logger.info("discoveryNode_id:{}, discoveryNode_name:{}, discoveryNode_address:{}",
            discoveryNode.id(), discoveryNode.name(), discoveryNode.address());

      }
    });
  }

  public interface NodeCallback<T> {

    T doWithNode(DiscoveryNode node) throws HermesException;
  }

  private class ScheduledNodesSampler implements Runnable {

    @Override
    public synchronized void run() {
      ImmutableList<DiscoveryNode> listedNodes = TransportClientNodesService.this.listedNodes;
      final CountDownLatch latch = new CountDownLatch(listedNodes.size());
      final CopyOnWriteArrayList<NodesInfoResponse> nodesInfoResponses =
          new CopyOnWriteArrayList<NodesInfoResponse>();
      for (final DiscoveryNode listedNode : listedNodes) {
        threadPool.execute(new Runnable() {
          @Override
          public void run() {
            try {
              transportService.connectToNode(listedNode); // make sure we are connected to it
              transportService.sendRequest(listedNode, TransportActions.Admin.Cluster.Node.INFO,
                  Requests.nodesInfo("_local"),
                  new BaseTransportResponseHandler<NodesInfoResponse>() {

                    @Override
                    public NodesInfoResponse newInstance() {
                      return new NodesInfoResponse();
                    }

                    @Override
                    public void handleResponse(NodesInfoResponse response) {
                      nodesInfoResponses.add(response);
                      latch.countDown();
                    }

                    @Override
                    public void handleException(RemoteTransportException exp) {
                      logger.debug("Failed to get node info from " + listedNode
                          + ", removed from nodes list", exp);
                      latch.countDown();
                    }
                  });
            } catch (Exception e) {
              logger.debug(
                  "Failed to get node info from " + listedNode + ", removed from nodes list", e);
              latch.countDown();
            }
          }
        });
      }

      try {
        latch.await();
      } catch (InterruptedException e) {
        return;
      }

      HashSet<DiscoveryNode> newNodes = new HashSet<DiscoveryNode>();
      for (NodesInfoResponse nodesInfoResponse : nodesInfoResponses) {
        if (nodesInfoResponse.nodes().length > 0) {
          DiscoveryNode node = nodesInfoResponse.nodes()[0].node();
          if (!clusterName.equals(nodesInfoResponse.clusterName())) {
            logger.warn("Node {} not part of the cluster {}, ignoring...", node, clusterName);
          } else {
            newNodes.add(node);
          }
        } else {
          // should not really happen....
          logger.debug("No info returned from node...");
        }
      }
      if (discoveredNodes != null) {
        newNodes.addAll(discoveredNodes.nodes().values());
      }
      // now, make sure we are connected to all the updated nodes
      for (DiscoveryNode node : newNodes) {
        try {
          transportService.connectToNode(node);
        } catch (Exception e) {
          logger.debug("Failed to connect to discovered node [" + node + "]", e);
        }
      }
      nodes = new ImmutableList.Builder<DiscoveryNode>().addAll(newNodes).build();
    }
  }
}
