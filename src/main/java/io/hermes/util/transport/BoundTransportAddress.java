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

package io.hermes.util.transport;

/**
 * A bounded transport address is a tuple of two {@link TransportAddress}, one that represents the
 * address the transport is bounded on, the the published one represents the one clients should
 * communicate on.
 *
 * @author spancer.ray
 */
public class BoundTransportAddress {

  private final TransportAddress boundAddress;

  private final TransportAddress publishAddress;

  public BoundTransportAddress(TransportAddress boundAddress, TransportAddress publishAddress) {
    this.boundAddress = boundAddress;
    this.publishAddress = publishAddress;
  }

  public TransportAddress boundAddress() {
    return boundAddress;
  }

  public TransportAddress publishAddress() {
    return publishAddress;
  }

  @Override
  public String toString() {
    return "bound_address[" + boundAddress + "], publish_address[" + publishAddress + "]";
  }
}