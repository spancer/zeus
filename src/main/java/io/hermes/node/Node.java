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

package io.hermes.node;

import io.hermes.client.Client;
import io.hermes.util.settings.Settings;

/**
 * A node represent a node within a cluster (<tt>cluster.name</tt>). The {@link #client()} can be
 * used in order to use a {@link Client} to perform actions/operations against the cluster.
 *
 * <p>
 * In order to create a node, the {@link NodeBuilder} can be used. When done with it, make sure to
 * call {@link #close()} on it.
 *
 * @author spancer.ray
 */
public interface Node {

  /**
   * The settings that were used to create the node.
   */
  Settings settings();

  /**
   * A client that can be used to execute actions (operations) against the cluster.
   */
  Client client();

  /**
   * Start the node. If the node is already started, this method is no-op.
   */
  Node start();

  /**
   * Stops the node. If the node is already started, this method is no-op.
   */
  Node stop();

  /**
   * Closes the node (and {@link #stop}s if its running).
   */
  void close();
}
