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

package io.hermes.action.admin.cluster.ping.broadcast;

import io.hermes.action.support.broadcast.BroadcastOperationRequest;
import io.hermes.action.support.broadcast.BroadcastOperationThreading;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class BroadcastPingRequest extends BroadcastOperationRequest {

  BroadcastPingRequest() {
  }

  public BroadcastPingRequest(String... indices) {
    super(indices, null);
  }

  @Override
  public BroadcastPingRequest operationThreading(BroadcastOperationThreading operationThreading) {
    super.operationThreading(operationThreading);
    return this;
  }

  @Override
  public BroadcastPingRequest listenerThreaded(boolean threadedListener) {
    super.listenerThreaded(threadedListener);
    return this;
  }

  public BroadcastPingRequest queryHint(String queryHint) {
    this.queryHint = queryHint;
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
