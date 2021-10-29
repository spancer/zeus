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

package io.hermes.cluster;

import io.hermes.cluster.node.DiscoveryNodes;

/**
 * @author spancer.ray
 */
public class ClusterChangedEvent {

  private final String source;

  private final ClusterState previousState;

  private final ClusterState state;

  private final boolean firstMaster;

  private final DiscoveryNodes.Delta nodesDelta;

  public ClusterChangedEvent(String source, ClusterState state, ClusterState previousState,
      boolean firstMaster) {
    this.source = source;
    this.state = state;
    this.previousState = previousState;
    this.firstMaster = firstMaster;
    this.nodesDelta = state.nodes().delta(previousState.nodes());
  }

  /**
   * The source that caused this cluster event to be raised.
   */
  public String source() {
    return this.source;
  }

  public ClusterState state() {
    return this.state;
  }

  public ClusterState previousState() {
    return this.previousState;
  }


  public boolean localNodeMaster() {
    return state.nodes().localNodeMaster();
  }

  public boolean firstMaster() {
    return firstMaster;
  }

  public DiscoveryNodes.Delta nodesDelta() {
    return this.nodesDelta;
  }

  public boolean nodesRemoved() {
    return nodesDelta.removed();
  }

  public boolean nodesAdded() {
    return nodesDelta.added();
  }

  public boolean nodesChanged() {
    return nodesRemoved() || nodesAdded();
  }
}
