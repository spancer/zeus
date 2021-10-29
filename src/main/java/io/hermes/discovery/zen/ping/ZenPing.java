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

package io.hermes.discovery.zen.ping;

import static io.hermes.cluster.ClusterName.readClusterName;
import static io.hermes.cluster.node.DiscoveryNode.readNode;

import io.hermes.HermesException;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.discovery.zen.DiscoveryNodesProvider;
import io.hermes.util.TimeValue;
import io.hermes.util.component.LifecycleComponent;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public interface ZenPing extends LifecycleComponent<ZenPing> {

  void setNodesProvider(DiscoveryNodesProvider nodesProvider);

  void ping(PingListener listener, TimeValue timeout) throws HermesException;

  interface PingListener {

    void onPing(PingResponse[] pings);
  }

  class PingResponse implements Streamable {

    private ClusterName clusterName;

    private DiscoveryNode target;

    private DiscoveryNode master;

    private PingResponse() {
    }

    public PingResponse(DiscoveryNode target, DiscoveryNode master, ClusterName clusterName) {
      this.target = target;
      this.master = master;
      this.clusterName = clusterName;
    }

    public static PingResponse readPingResponse(StreamInput in) throws IOException {
      PingResponse response = new PingResponse();
      response.readFrom(in);
      return response;
    }

    public ClusterName clusterName() {
      return this.clusterName;
    }

    public DiscoveryNode target() {
      return target;
    }

    public DiscoveryNode master() {
      return master;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      clusterName = readClusterName(in);
      target = readNode(in);
      if (in.readBoolean()) {
        master = readNode(in);
      }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      clusterName.writeTo(out);
      target.writeTo(out);
      if (master == null) {
        out.writeBoolean(false);
      } else {
        out.writeBoolean(true);
        master.writeTo(out);
      }
    }

    @Override
    public String toString() {
      return "ping_response target [" + target + "], master [" + master + "]";
    }
  }
}
