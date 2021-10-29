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

package io.hermes.cluster;

import io.hermes.cluster.metadata.MetaData;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.util.Nullable;
import io.hermes.util.io.stream.BytesStreamInput;
import io.hermes.util.io.stream.BytesStreamOutput;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.settings.Settings;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class ClusterState {

  private final long version;

  private final DiscoveryNodes nodes;

  private final MetaData metaData;


  public ClusterState(long version, MetaData metaData, DiscoveryNodes nodes) {
    this.version = version;
    this.metaData = metaData;
    this.nodes = nodes;
  }

  public static Builder newClusterStateBuilder() {
    return new Builder();
  }

  public long version() {
    return this.version;
  }

  public long getVersion() {
    return version();
  }

  public DiscoveryNodes nodes() {
    return this.nodes;
  }

  public DiscoveryNodes getNodes() {
    return nodes();
  }

  public MetaData metaData() {
    return this.metaData;
  }

  public MetaData getMetaData() {
    return metaData();
  }

  public static class Builder {

    private long version = 0;

    private MetaData metaData = MetaData.EMPTY_META_DATA;

    private DiscoveryNodes nodes = DiscoveryNodes.EMPTY_NODES;

    public static byte[] toBytes(ClusterState state) throws IOException {
      BytesStreamOutput os = BytesStreamOutput.Cached.cached();
      writeTo(state, os);
      return os.copiedByteArray();
    }

    public static ClusterState fromBytes(byte[] data, Settings globalSettings,
        DiscoveryNode localNode) throws IOException {
      return readFrom(new BytesStreamInput(data), globalSettings, localNode);
    }

    public static void writeTo(ClusterState state, StreamOutput out) throws IOException {
      out.writeLong(state.version());
      MetaData.Builder.writeTo(state.metaData(), out);
      DiscoveryNodes.Builder.writeTo(state.nodes(), out);
    }

    public static ClusterState readFrom(StreamInput in, @Nullable Settings globalSettings,
        @Nullable DiscoveryNode localNode) throws IOException {
      Builder builder = new Builder();
      builder.version = in.readLong();
      builder.metaData = MetaData.Builder.readFrom(in, globalSettings);
      builder.nodes = DiscoveryNodes.Builder.readFrom(in, localNode);
      return builder.build();
    }

    public Builder nodes(DiscoveryNodes.Builder nodesBuilder) {
      return nodes(nodesBuilder.build());
    }

    public Builder nodes(DiscoveryNodes nodes) {
      this.nodes = nodes;
      return this;
    }

    public Builder metaData(MetaData.Builder metaDataBuilder) {
      return metaData(metaDataBuilder.build());
    }

    public Builder metaData(MetaData metaData) {
      this.metaData = metaData;
      return this;
    }

    public Builder state(ClusterState state) {
      this.version = state.version();
      this.nodes = state.nodes();
      this.metaData = state.metaData();
      return this;
    }

    public ClusterState build() {
      return new ClusterState(version, metaData, nodes);
    }
  }
}
