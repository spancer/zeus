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

package io.hermes.action.admin.cluster.node.shutdown;

import static io.hermes.util.TimeValue.readTimeValue;

import io.hermes.action.support.nodes.NodesOperationRequest;
import io.hermes.util.TimeValue;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * A request to shutdown one ore more nodes (or the whole cluster).
 *
 * @author spancer.ray
 */
public class NodesShutdownRequest extends NodesOperationRequest {

  TimeValue delay = TimeValue.timeValueSeconds(1);

  protected NodesShutdownRequest() {
  }

  /**
   * Shuts down nodes based on the nodes ids specified. If none are passed, <b>all</b> nodes will be
   * shutdown.
   */
  public NodesShutdownRequest(String... nodesIds) {
    super(nodesIds);
  }

  /**
   * The delay for the shutdown to occur. Defaults to <tt>1s</tt>.
   */
  public NodesShutdownRequest delay(TimeValue delay) {
    this.delay = delay;
    return this;
  }

  public TimeValue delay() {
    return this.delay;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    super.readFrom(in);
    delay = readTimeValue(in);
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    super.writeTo(out);
    delay.writeTo(out);
  }
}
