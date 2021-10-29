/*******************************************************************************
 * Copyright 2021 spancer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/*
 * Copyright (c) Chalco-Steering Technology Co., Ltd. All Rights Reserved. This software is licensed
 * not sold. Use or reproduction of this software by any unauthorized individual or entity is
 * strictly prohibited. This software is the confidential and proprietary information of
 * Chalco-Steering Technology Co., Ltd. Disclosure of such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into with Chalco-Steering
 * Technology Co., Ltd. Chalco-Steering Technology Co., Ltd. MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * Chalco-Steering Technology Co., Ltd. SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ANY DERIVATIVES THEREOF.
 */


package io.hermes.client;

import io.hermes.action.admin.cluster.health.ClusterHealthRequest;
import io.hermes.action.admin.cluster.node.info.NodesInfoRequest;
import io.hermes.action.admin.cluster.node.shutdown.NodesShutdownRequest;
import io.hermes.action.admin.cluster.ping.broadcast.BroadcastPingRequest;
import io.hermes.action.admin.cluster.ping.single.SinglePingRequest;
import io.hermes.action.admin.cluster.state.ClusterStateRequest;

/**
 * A handy one stop shop for creating requests (make sure to import static this class).
 *
 * @author spancer.ray
 */
public class Requests {



  /**
   * Creates a cluster state request.
   *
   * @return The cluster state request.
   */
  public static ClusterStateRequest clusterState() {
    return new ClusterStateRequest();
  }

  /**
   * Creates a cluster health request.
   *
   * @param indices The indices to optimize. Use <tt>null</tt> or <tt>_all</tt> to execute against
   *        all indices
   * @return The cluster health request
   */
  public static ClusterHealthRequest clusterHealth(String... indices) {
    return new ClusterHealthRequest(indices);
  }

  /**
   * Creates a nodes info request against all the nodes.
   *
   * @return The nodes info request
   */
  public static NodesInfoRequest nodesInfo() {
    return new NodesInfoRequest();
  }

  /**
   * Creates a nodes info request against one or more nodes. Pass <tt>null</tt> or an empty array
   * for all nodes.
   *
   * @param nodesIds The nodes ids to get the status for
   * @return The nodes info request
   */
  public static NodesInfoRequest nodesInfo(String... nodesIds) {
    return new NodesInfoRequest(nodesIds);
  }

  /**
   * Shuts down all nodes in the cluster.
   */
  public static NodesShutdownRequest nodesShutdown() {
    return new NodesShutdownRequest();
  }

  /**
   * Shuts down the specified nodes in the cluster.
   *
   * @param nodesIds The nodes ids to get the status for
   * @return The nodes info request
   */
  public static NodesShutdownRequest nodesShutdown(String... nodesIds) {
    return new NodesShutdownRequest(nodesIds);
  }

  public static SinglePingRequest pingSingleRequest(String index) {
    return new SinglePingRequest(index);
  }

  public static BroadcastPingRequest pingBroadcastRequest(String... indices) {
    return new BroadcastPingRequest(indices);
  }

}
