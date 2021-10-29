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



package io.hermes.client;

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

/**
 * Administrative actions/operations against indices.
 *
 * @author spancer.ray
 * @see AdminClient#cluster()
 */
public interface ClusterAdminClient {

  /**
   * The health of the cluster.
   *
   * @param request The cluster state request
   * @return The result future
   * @see Requests#clusterHealth(String...)
   */
  ActionFuture<ClusterHealthResponse> health(ClusterHealthRequest request);

  /**
   * The health of the cluster.
   *
   * @param request  The cluster state request
   * @param listener A listener to be notified with a result
   * @see Requests#clusterHealth(String...)
   */
  void health(ClusterHealthRequest request, ActionListener<ClusterHealthResponse> listener);

  /**
   * The state of the cluster.
   *
   * @param request The cluster state request.
   * @return The result future
   * @see Requests#clusterState()
   */
  ActionFuture<ClusterStateResponse> state(ClusterStateRequest request);

  /**
   * The state of the cluster.
   *
   * @param request  The cluster state request.
   * @param listener A listener to be notified with a result
   * @see Requests#clusterState()
   */
  void state(ClusterStateRequest request, ActionListener<ClusterStateResponse> listener);

  /**
   * Nodes info of the cluster.
   *
   * @param request The nodes info request
   * @return The result future
   */
  ActionFuture<NodesInfoResponse> nodesInfo(NodesInfoRequest request);

  /**
   * Nodes info of the cluster.
   *
   * @param request  The nodes info request
   * @param listener A listener to be notified with a result
   */
  void nodesInfo(NodesInfoRequest request, ActionListener<NodesInfoResponse> listener);

  /**
   * Shutdown nodes in the cluster.
   *
   * @param request The nodes shutdown request
   * @return The result future
   */
  ActionFuture<NodesShutdownResponse> nodesShutdown(NodesShutdownRequest request);

  /**
   * Shutdown nodes in the cluster.
   *
   * @param request  The nodes shutdown request
   * @param listener A listener to be notified with a result
   */
  void nodesShutdown(NodesShutdownRequest request, ActionListener<NodesShutdownResponse> listener);

  ActionFuture<SinglePingResponse> ping(SinglePingRequest request);

  void ping(SinglePingRequest request, ActionListener<SinglePingResponse> listener);

  ActionFuture<BroadcastPingResponse> ping(BroadcastPingRequest request);

  void ping(BroadcastPingRequest request, ActionListener<BroadcastPingResponse> listener);

}
