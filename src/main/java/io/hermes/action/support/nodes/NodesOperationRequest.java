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

import io.hermes.action.ActionRequest;
import io.hermes.action.ActionRequestValidationException;
import io.hermes.util.Strings;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public abstract class NodesOperationRequest implements ActionRequest {

  public static String[] ALL_NODES = Strings.EMPTY_ARRAY;

  private String[] nodesIds;

  private boolean listenerThreaded = false;

  protected NodesOperationRequest() {

  }

  protected NodesOperationRequest(String... nodesIds) {
    this.nodesIds = nodesIds;
  }

  @Override
  public NodesOperationRequest listenerThreaded(boolean listenerThreaded) {
    this.listenerThreaded = listenerThreaded;
    return this;
  }

  @Override
  public boolean listenerThreaded() {
    return this.listenerThreaded;
  }

  public String[] nodesIds() {
    return nodesIds;
  }

  public NodesOperationRequest nodesIds(String... nodesIds) {
    this.nodesIds = nodesIds;
    return this;
  }

  @Override
  public ActionRequestValidationException validate() {
    return null;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    nodesIds = new String[in.readVInt()];
    for (int i = 0; i < nodesIds.length; i++) {
      nodesIds[i] = in.readUTF();
    }
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    if (nodesIds == null) {
      out.writeVInt(0);
    } else {
      out.writeVInt(nodesIds.length);
      for (String nodeId : nodesIds) {
        out.writeUTF(nodeId);
      }
    }
  }
}
