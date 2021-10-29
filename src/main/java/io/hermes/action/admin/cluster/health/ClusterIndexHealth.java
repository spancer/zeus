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

import static io.hermes.action.admin.cluster.health.ClusterShardHealth.readClusterShardHealth;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author spancer.ray
 */
public class ClusterIndexHealth implements Iterable<ClusterShardHealth>, Streamable {

  final Map<Integer, ClusterShardHealth> shards = Maps.newHashMap();
  int activeShards = 0;
  int relocatingShards = 0;
  int activePrimaryShards = 0;
  ClusterHealthStatus status = ClusterHealthStatus.RED;
  List<String> validationFailures;
  private String index;
  private int numberOfShards;
  private int numberOfReplicas;

  private ClusterIndexHealth() {
  }

  public ClusterIndexHealth(String index, int numberOfShards, int numberOfReplicas,
      List<String> validationFailures) {
    this.index = index;
    this.numberOfShards = numberOfShards;
    this.numberOfReplicas = numberOfReplicas;
    this.validationFailures = validationFailures;
  }

  public static ClusterIndexHealth readClusterIndexHealth(StreamInput in) throws IOException {
    ClusterIndexHealth indexHealth = new ClusterIndexHealth();
    indexHealth.readFrom(in);
    return indexHealth;
  }

  public String index() {
    return index;
  }

  public String getIndex() {
    return index();
  }

  public List<String> validationFailures() {
    return this.validationFailures;
  }

  public List<String> getValidationFailures() {
    return validationFailures();
  }

  public int numberOfShards() {
    return numberOfShards;
  }

  public int getNumberOfShards() {
    return numberOfShards();
  }

  public int numberOfReplicas() {
    return numberOfReplicas;
  }

  public int getNumberOfReplicas() {
    return numberOfReplicas();
  }

  public int activeShards() {
    return activeShards;
  }

  public int getActiveShards() {
    return activeShards();
  }

  public int relocatingShards() {
    return relocatingShards;
  }

  public int getRelocatingShards() {
    return relocatingShards();
  }

  public int activePrimaryShards() {
    return activePrimaryShards;
  }

  public int getActivePrimaryShards() {
    return activePrimaryShards();
  }

  public ClusterHealthStatus status() {
    return status;
  }

  public ClusterHealthStatus getStatus() {
    return status();
  }

  public Map<Integer, ClusterShardHealth> shards() {
    return this.shards;
  }

  public Map<Integer, ClusterShardHealth> getShards() {
    return shards();
  }

  @Override
  public Iterator<ClusterShardHealth> iterator() {
    return shards.values().iterator();
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    index = in.readUTF();
    numberOfShards = in.readVInt();
    numberOfReplicas = in.readVInt();
    activePrimaryShards = in.readVInt();
    activeShards = in.readVInt();
    relocatingShards = in.readVInt();
    status = ClusterHealthStatus.fromValue(in.readByte());

    int size = in.readVInt();
    for (int i = 0; i < size; i++) {
      ClusterShardHealth shardHealth = readClusterShardHealth(in);
      shards.put(shardHealth.id(), shardHealth);
    }
    size = in.readVInt();
    if (size == 0) {
      validationFailures = ImmutableList.of();
    } else {
      for (int i = 0; i < size; i++) {
        validationFailures.add(in.readUTF());
      }
    }
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeUTF(index);
    out.writeVInt(numberOfShards);
    out.writeVInt(numberOfReplicas);
    out.writeVInt(activePrimaryShards);
    out.writeVInt(activeShards);
    out.writeVInt(relocatingShards);
    out.writeByte(status.value());

    out.writeVInt(shards.size());
    for (ClusterShardHealth shardHealth : this) {
      shardHealth.writeTo(out);
    }

    out.writeVInt(validationFailures.size());
    for (String failure : validationFailures) {
      out.writeUTF(failure);
    }
  }
}
