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

import io.hermes.action.support.nodes.NodesOperationResponse;
import io.hermes.cluster.ClusterName;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class NodesInfoResponse extends NodesOperationResponse<NodeInfo> {

  public NodesInfoResponse() {
  }

  public NodesInfoResponse(ClusterName clusterName, NodeInfo[] nodes) {
    super(clusterName, nodes);
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    super.readFrom(in);
    nodes = new NodeInfo[in.readVInt()];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = NodeInfo.readNodeInfo(in);
    }
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    super.writeTo(out);
    out.writeVInt(nodes.length);
    for (NodeInfo node : nodes) {
      node.writeTo(out);
    }
  }
}
