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

package io.hermes.action.support.nodes;

import com.google.common.collect.Maps;
import io.hermes.action.ActionResponse;
import io.hermes.cluster.ClusterName;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author spancer.ray
 */
public abstract class NodesOperationResponse<NodeResponse extends NodeOperationResponse> implements
    ActionResponse, Iterable<NodeResponse> {

  protected NodeResponse[] nodes;
  private ClusterName clusterName;
  private Map<String, NodeResponse> nodesMap;

  protected NodesOperationResponse() {
  }

  protected NodesOperationResponse(ClusterName clusterName, NodeResponse[] nodes) {
    this.clusterName = clusterName;
    this.nodes = nodes;
  }

  public ClusterName clusterName() {
    return this.clusterName;
  }

  public String getClusterName() {
    return clusterName().value();
  }

  public NodeResponse[] nodes() {
    return nodes;
  }

  public NodeResponse[] getNodes() {
    return nodes();
  }

  public NodeResponse getAt(int position) {
    return nodes[position];
  }

  @Override
  public Iterator<NodeResponse> iterator() {
    return nodesMap().values().iterator();
  }

  public Map<String, NodeResponse> nodesMap() {
    if (nodesMap == null) {
      nodesMap = Maps.newHashMap();
      for (NodeResponse nodeResponse : nodes) {
        nodesMap.put(nodeResponse.node().id(), nodeResponse);
      }
    }
    return nodesMap;
  }

  public Map<String, NodeResponse> getNodesMap() {
    return nodesMap();
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    clusterName = ClusterName.readClusterName(in);
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    clusterName.writeTo(out);
  }
}
