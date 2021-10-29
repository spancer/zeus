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

package io.hermes.action.admin.cluster.node.shutdown;

import io.hermes.action.support.nodes.NodeOperationResponse;
import io.hermes.action.support.nodes.NodesOperationResponse;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class NodesShutdownResponse extends
    NodesOperationResponse<NodesShutdownResponse.NodeShutdownResponse> {

  NodesShutdownResponse() {
  }

  public NodesShutdownResponse(ClusterName clusterName, NodeShutdownResponse[] nodes) {
    super(clusterName, nodes);
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    super.readFrom(in);
    nodes = new NodeShutdownResponse[in.readVInt()];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = NodeShutdownResponse.readNodeShutdownResponse(in);
    }
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    super.writeTo(out);
    out.writeVInt(nodes.length);
    for (NodeShutdownResponse node : nodes) {
      node.writeTo(out);
    }
  }

  public static class NodeShutdownResponse extends NodeOperationResponse {

    NodeShutdownResponse() {
    }

    public NodeShutdownResponse(DiscoveryNode node) {
      super(node);
    }

    public static NodeShutdownResponse readNodeShutdownResponse(StreamInput in) throws IOException {
      NodeShutdownResponse res = new NodeShutdownResponse();
      res.readFrom(in);
      return res;
    }
  }
}
