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

package io.hermes.action.admin.cluster.state;

import io.hermes.action.ActionResponse;
import io.hermes.cluster.ClusterState;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class ClusterStateResponse implements ActionResponse {

  private ClusterState clusterState;

  ClusterStateResponse() {
  }

  ClusterStateResponse(ClusterState clusterState) {
    this.clusterState = clusterState;
  }

  public ClusterState state() {
    return this.clusterState;
  }

  public ClusterState getState() {
    return state();
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    clusterState = ClusterState.Builder.readFrom(in, null, null);
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    ClusterState.Builder.writeTo(clusterState, out);
  }
}
