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



package io.hermes.client.transport.support;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.action.ActionFuture;
import io.hermes.action.ActionListener;
import io.hermes.action.admin.cluster.health.ClusterHealthRequest;
import io.hermes.action.admin.cluster.health.ClusterHealthResponse;
import io.hermes.action.admin.cluster.node.info.NodesInfoRequest;
import io.hermes.action.admin.cluster.node.info.NodesInfoResponse;
import io.hermes.action.admin.cluster.node.shutdown.NodesShutdownRequest;
import io.hermes.action.admin.cluster.node.shutdown.NodesShutdownResponse;
import io.hermes.action.admin.cluster.ping.broadcast.BroadcastPingRequest;
import io.hermes.action.admin.cluster.ping.broadcast.BroadcastPingResponse;
import io.hermes.action.admin.cluster.ping.single.SinglePingRequest;
import io.hermes.action.admin.cluster.ping.single.SinglePingResponse;
import io.hermes.action.admin.cluster.state.ClusterStateRequest;
import io.hermes.action.admin.cluster.state.ClusterStateResponse;
import io.hermes.client.ClusterAdminClient;
import io.hermes.client.transport.TransportClientNodesService;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class InternalTransportClusterAdminClient extends AbstractComponent
    implements ClusterAdminClient {

  private final TransportClientNodesService nodesService;

  @Inject
  public InternalTransportClusterAdminClient(Settings settings,
      TransportClientNodesService nodesService
  ) {
    super(settings);
    this.nodesService = nodesService;
  }

  @Override
  public ActionFuture<ClusterHealthResponse> health(final ClusterHealthRequest request) {
    return nodesService.execute(
        new TransportClientNodesService.NodeCallback<ActionFuture<ClusterHealthResponse>>() {
          @Override
          public ActionFuture<ClusterHealthResponse> doWithNode(DiscoveryNode node)
              throws HermesException {
//            return clusterHealthAction.execute(node, request);
            return null;

          }
        });
  }

  @Override
  public void health(final ClusterHealthRequest request,
      final ActionListener<ClusterHealthResponse> listener) {
    nodesService.execute(new TransportClientNodesService.NodeCallback<Void>() {
      @Override
      public Void doWithNode(DiscoveryNode node) throws HermesException {
//        clusterHealthAction.execute(node, request, listener);
        return null;
      }
    });
  }

  @Override
  public ActionFuture<ClusterStateResponse> state(final ClusterStateRequest request) {
    return nodesService.execute(
        new TransportClientNodesService.NodeCallback<ActionFuture<ClusterStateResponse>>() {
          @Override
          public ActionFuture<ClusterStateResponse> doWithNode(DiscoveryNode node)
              throws HermesException {
//            return clusterStateAction.execute(node, request);
            return null;

          }
        });
  }

  @Override
  public void state(final ClusterStateRequest request,
      final ActionListener<ClusterStateResponse> listener) {
    nodesService.execute(new TransportClientNodesService.NodeCallback<Void>() {
      @Override
      public Void doWithNode(DiscoveryNode node) throws HermesException {
//        clusterStateAction.execute(node, request, listener);
        return null;
      }
    });
  }

  @Override
  public ActionFuture<SinglePingResponse> ping(final SinglePingRequest request) {
    return nodesService
        .execute(new TransportClientNodesService.NodeCallback<ActionFuture<SinglePingResponse>>() {
          @Override
          public ActionFuture<SinglePingResponse> doWithNode(DiscoveryNode node)
              throws HermesException {
//            return singlePingAction.execute(node, request);
            return null;
          }
        });
  }

  @Override
  public void ping(final SinglePingRequest request,
      final ActionListener<SinglePingResponse> listener) {
    nodesService.execute(new TransportClientNodesService.NodeCallback<Void>() {
      @Override
      public Void doWithNode(DiscoveryNode node) throws HermesException {
//        singlePingAction.execute(node, request, listener);
        return null;
      }
    });
  }

  @Override
  public ActionFuture<BroadcastPingResponse> ping(final BroadcastPingRequest request) {
    return nodesService.execute(
        new TransportClientNodesService.NodeCallback<ActionFuture<BroadcastPingResponse>>() {
          @Override
          public ActionFuture<BroadcastPingResponse> doWithNode(DiscoveryNode node)
              throws HermesException {
//            return broadcastPingAction.execute(node, request);
            return null;

          }
        });
  }

  @Override
  public void ping(final BroadcastPingRequest request,
      final ActionListener<BroadcastPingResponse> listener) {
    nodesService.execute(new TransportClientNodesService.NodeCallback<Void>() {
      @Override
      public Void doWithNode(DiscoveryNode node) throws HermesException {
//        broadcastPingAction.execute(node, request, listener);
        return null;
      }
    });
  }


  @Override
  public ActionFuture<NodesInfoResponse> nodesInfo(final NodesInfoRequest request) {
    return nodesService
        .execute(new TransportClientNodesService.NodeCallback<ActionFuture<NodesInfoResponse>>() {
          @Override
          public ActionFuture<NodesInfoResponse> doWithNode(DiscoveryNode node)
              throws HermesException {
//            return nodesInfoAction.execute(node, request);
            return null;
          }
        });
  }

  @Override
  public void nodesInfo(final NodesInfoRequest request,
      final ActionListener<NodesInfoResponse> listener) {
    nodesService.execute(new TransportClientNodesService.NodeCallback<Void>() {
      @Override
      public Void doWithNode(DiscoveryNode node) throws HermesException {
//        nodesInfoAction.execute(node, request, listener);
        return null;
      }
    });
  }

  @Override
  public ActionFuture<NodesShutdownResponse> nodesShutdown(final NodesShutdownRequest request) {
    return nodesService.execute(
        new TransportClientNodesService.NodeCallback<ActionFuture<NodesShutdownResponse>>() {
          @Override
          public ActionFuture<NodesShutdownResponse> doWithNode(DiscoveryNode node)
              throws HermesException {
//            return nodesShutdownAction.execute(node, request);
            return null;

          }
        });
  }

  @Override
  public void nodesShutdown(final NodesShutdownRequest request,
      final ActionListener<NodesShutdownResponse> listener) {
    nodesService.execute(new TransportClientNodesService.NodeCallback<ActionFuture<Void>>() {
      @Override
      public ActionFuture<Void> doWithNode(DiscoveryNode node) throws HermesException {
//        nodesShutdownAction.execute(node, request, listener);
        return null;
      }
    });
  }
}
