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

package io.hermes.action.admin.cluster.node.info;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.action.TransportActions;
import io.hermes.action.support.nodes.NodeOperationRequest;
import io.hermes.action.support.nodes.TransportNodesOperationAction;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.ClusterService;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.TransportService;
import io.hermes.util.MapBuilder;
import io.hermes.util.settings.Settings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author spancer.ray
 */
public class TransportNodesInfo extends
    TransportNodesOperationAction<NodesInfoRequest, NodesInfoResponse, TransportNodesInfo.NodeInfoRequest, NodeInfo> {

  private volatile ImmutableMap<String, String> nodeAttributes = ImmutableMap.of();

  @Inject
  public TransportNodesInfo(Settings settings, ClusterName clusterName, ThreadPool threadPool,
      ClusterService clusterService, TransportService transportService) {
    super(settings, clusterName, threadPool, clusterService, transportService);
  }

  public synchronized void putNodeAttribute(String key, String value) {
    nodeAttributes = new MapBuilder<String, String>().putAll(nodeAttributes).put(key, value)
        .immutableMap();
  }

  public synchronized void removeNodeAttribute(String key) {
    nodeAttributes = new MapBuilder<String, String>().putAll(nodeAttributes).remove(key)
        .immutableMap();
  }

  @Override
  protected String transportAction() {
    return TransportActions.Admin.Cluster.Node.INFO;
  }

  @Override
  protected String transportNodeAction() {
    return "/cluster/nodes/info/node";
  }

  @Override
  protected NodesInfoResponse newResponse(NodesInfoRequest nodesInfoRequest,
      AtomicReferenceArray responses) {
    final List<NodeInfo> nodesInfos = new ArrayList<NodeInfo>();
    for (int i = 0; i < responses.length(); i++) {
      Object resp = responses.get(i);
      if (resp instanceof NodeInfo) {
        nodesInfos.add((NodeInfo) resp);
      }
    }
    return new NodesInfoResponse(clusterName, nodesInfos.toArray(new NodeInfo[nodesInfos.size()]));
  }

  @Override
  protected NodesInfoRequest newRequest() {
    return new NodesInfoRequest();
  }

  @Override
  protected NodeInfoRequest newNodeRequest() {
    return new NodeInfoRequest();
  }

  @Override
  protected NodeInfoRequest newNodeRequest(String nodeId, NodesInfoRequest request) {
    return new NodeInfoRequest(nodeId);
  }

  @Override
  protected NodeInfo newNodeResponse() {
    return new NodeInfo();
  }

  @Override
  protected NodeInfo nodeOperation(NodeInfoRequest nodeInfoRequest) throws HermesException {
    return new NodeInfo(clusterService.state().nodes().localNode(), nodeAttributes, settings);
  }

  @Override
  protected boolean accumulateExceptions() {
    return false;
  }

  protected static class NodeInfoRequest extends NodeOperationRequest {

    private NodeInfoRequest() {
    }

    private NodeInfoRequest(String nodeId) {
      super(nodeId);
    }
  }
}
