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

package io.hermes.action.admin.cluster.health;

import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class ClusterShardHealth implements Streamable {

  ClusterHealthStatus status = ClusterHealthStatus.RED;
  int activeShards = 0;
  int relocatingShards = 0;
  boolean primaryActive = false;
  private int shardId;

  private ClusterShardHealth() {

  }

  ClusterShardHealth(int shardId) {
    this.shardId = shardId;
  }

  static ClusterShardHealth readClusterShardHealth(StreamInput in) throws IOException {
    ClusterShardHealth ret = new ClusterShardHealth();
    ret.readFrom(in);
    return ret;
  }

  public int id() {
    return shardId;
  }

  public int getId() {
    return id();
  }

  public ClusterHealthStatus status() {
    return status;
  }

  public ClusterHealthStatus getStatus() {
    return status();
  }

  public int relocatingShards() {
    return relocatingShards;
  }

  public int getRelocatingShards() {
    return relocatingShards();
  }

  public int activeShards() {
    return activeShards;
  }

  public int getActiveShards() {
    return activeShards();
  }

  public boolean primaryActive() {
    return primaryActive;
  }

  public boolean isPrimaryActive() {
    return primaryActive();
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    shardId = in.readVInt();
    status = ClusterHealthStatus.fromValue(in.readByte());
    activeShards = in.readVInt();
    relocatingShards = in.readVInt();
    primaryActive = in.readBoolean();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeVInt(shardId);
    out.writeByte(status.value());
    out.writeVInt(activeShards);
    out.writeVInt(relocatingShards);
    out.writeBoolean(primaryActive);
  }
}
