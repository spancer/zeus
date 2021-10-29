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

package io.hermes.transport;

import io.hermes.cluster.node.DiscoveryNode;

/**
 * @author spancer.ray
 */
public class ConnectTransportException extends RemoteTransportException {

  private final DiscoveryNode node;

  public ConnectTransportException(DiscoveryNode node, String msg) {
    this(node, msg, null, null);
  }

  public ConnectTransportException(DiscoveryNode node, String msg, String action) {
    this(node, msg, action, null);
  }

  public ConnectTransportException(DiscoveryNode node, String msg, Throwable cause) {
    this(node, msg, null, cause);
  }

  public ConnectTransportException(DiscoveryNode node, String msg, String action, Throwable cause) {
    super(node.name(), node.address(), action, cause);
    this.node = node;
  }

  public DiscoveryNode node() {
    return node;
  }
}
