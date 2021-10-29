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

import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author spancer.ray
 */
public class InetSocketTransportAddress implements TransportAddress {

  private InetSocketAddress address;

  InetSocketTransportAddress() {

  }

  public InetSocketTransportAddress(String hostname, int port) {
    this(new InetSocketAddress(hostname, port));
  }

  public InetSocketTransportAddress(InetSocketAddress address) {
    this.address = address;
  }

  public static InetSocketTransportAddress readInetSocketTransportAddress(StreamInput in)
      throws IOException {
    InetSocketTransportAddress address = new InetSocketTransportAddress();
    address.readFrom(in);
    return address;
  }

  @Override
  public short uniqueAddressTypeId() {
    return 1;
  }

  public InetSocketAddress address() {
    return this.address;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    address = new InetSocketAddress(in.readUTF(), in.readInt());
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeUTF(address.getHostName());
    out.writeInt(address.getPort());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InetSocketTransportAddress address1 = (InetSocketTransportAddress) o;
    return address.equals(address1.address);
  }

  @Override
  public int hashCode() {
    return address != null ? address.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "inet[" + address + "]";
  }
}
