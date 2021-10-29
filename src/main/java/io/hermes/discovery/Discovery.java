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

package io.hermes.discovery;

import io.hermes.cluster.ClusterState;
import io.hermes.util.component.LifecycleComponent;

/**
 * A pluggable module allowing to implement discovery of other nodes, publishing of the cluster
 * state to all nodes, electing a master of the cluster that raises cluster state change events.
 *
 * @author spancer.ray
 */
public interface Discovery extends LifecycleComponent<Discovery> {

  void addListener(InitialStateDiscoveryListener listener);

  void removeListener(InitialStateDiscoveryListener listener);

  String nodeDescription();

  /**
   * Is the discovery of this node caused this node to be the first master in the cluster.
   */
  boolean firstMaster();

  /**
   * Publish all the changes to the cluster from the master (can be called just by the master). The
   * publish process should not publish this state to the master as well! (the master is sending
   * it...).
   */
  void publish(ClusterState clusterState);
}
