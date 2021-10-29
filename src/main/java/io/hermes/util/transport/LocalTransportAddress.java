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

/**
 * @author spancer.ray
 */
public class LocalTransportAddress implements TransportAddress {

  private String id;

  LocalTransportAddress() {
  }

  public LocalTransportAddress(String id) {
    this.id = id;
  }

  public String id() {
    return this.id;
  }

  @Override
  public short uniqueAddressTypeId() {
    return 2;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    id = in.readUTF();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeUTF(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LocalTransportAddress that = (LocalTransportAddress) o;

    return id != null ? id.equals(that.id) : that.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "local[" + id + "]";
  }
}
