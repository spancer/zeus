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

package io.hermes.action.admin.cluster.ping.single;

import io.hermes.action.support.single.SingleOperationRequest;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class SinglePingRequest extends SingleOperationRequest {

  public SinglePingRequest(String index) {
    super(index, null, null);
  }

  public SinglePingRequest(String index, String type, String id) {
    super(index, type, id);
  }

  SinglePingRequest() {
  }

  public SinglePingRequest type(String type) {
    this.type = type;
    return this;
  }

  public SinglePingRequest id(String id) {
    this.id = id;
    return this;
  }

  @Override
  public SinglePingRequest listenerThreaded(boolean threadedListener) {
    super.listenerThreaded(threadedListener);
    return this;
  }


  @Override
  public void readFrom(StreamInput in) throws IOException {
    super.readFrom(in);
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    super.writeTo(out);
  }

}
