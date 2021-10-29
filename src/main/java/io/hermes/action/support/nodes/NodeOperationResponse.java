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

import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public abstract class NodeOperationResponse implements Streamable {

  private DiscoveryNode node;

  protected NodeOperationResponse() {
  }

  protected NodeOperationResponse(DiscoveryNode node) {
    this.node = node;
  }

  public DiscoveryNode node() {
    return node;
  }

  public DiscoveryNode getNode() {
    return node();
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    node = DiscoveryNode.readNode(in);
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    node.writeTo(out);
  }
}
