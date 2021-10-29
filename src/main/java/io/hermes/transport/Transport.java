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
import io.hermes.util.component.LifecycleComponent;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.transport.BoundTransportAddress;
import io.hermes.util.transport.TransportAddress;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public interface Transport extends LifecycleComponent<Transport> {

  void transportServiceAdapter(TransportServiceAdapter service);

  /**
   * The address the transport is bound on.
   */
  BoundTransportAddress boundAddress();

  /**
   * Returns an address from its string representation.
   */
  TransportAddress addressFromString(String address) throws Exception;

  /**
   * Is the address type supported.
   */
  boolean addressSupported(Class<? extends TransportAddress> address);

  /**
   * Returns <tt>true</tt> if the node is connected.
   */
  boolean nodeConnected(DiscoveryNode node);

  /**
   * Connects to the given node, if already connected, does nothing.
   */
  void connectToNode(DiscoveryNode node) throws ConnectTransportException;

  /**
   * Disconnected from the given node, if not connected, will do nothing.
   */
  void disconnectFromNode(DiscoveryNode node);

  <T extends Streamable> void sendRequest(DiscoveryNode node, long requestId, String action,
      Streamable message, TransportResponseHandler<T> handler)
      throws IOException, TransportException;

  class Helper {

    public static final byte TRANSPORT_TYPE = 1;
    public static final byte RESPONSE_TYPE = 1 << 1;

    public static boolean isRequest(byte value) {
      return (value & TRANSPORT_TYPE) == 0;
    }

    public static byte setRequest(byte value) {
      value &= ~TRANSPORT_TYPE;
      return value;
    }

    public static byte setResponse(byte value) {
      value |= TRANSPORT_TYPE;
      return value;
    }

    public static boolean isError(byte value) {
      return (value & RESPONSE_TYPE) != 0;
    }

    public static byte setError(byte value) {
      value |= RESPONSE_TYPE;
      return value;
    }

  }
}
