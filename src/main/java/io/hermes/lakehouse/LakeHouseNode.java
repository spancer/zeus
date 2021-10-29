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


package io.hermes.lakehouse;

import com.google.common.collect.ImmutableMap;
import io.hermes.lakespace.LakeSpace;
import io.hermes.laketable.LakeTable;
import io.hermes.util.concurrent.Immutable;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;
import java.io.Serializable;

/**
 * Allows for node level components to be injected with the node id.
 *
 * @author spancer.ray
 */
@Immutable
public class LakeHouseNode implements Serializable, Streamable {

  private LakeHouse lakeHouse;
  private ImmutableMap<String, LakeSpace> lakeSpaces;
  private ImmutableMap<LakeSpace, LakeTable> spaceTables;
  private int nodeId;

  // TODO needs to construct using fields.
  private LakeHouseNode() {

  }

  public static LakeHouseNode readShardId(StreamInput in) throws IOException {
    LakeHouseNode shardId = new LakeHouseNode();
    shardId.readFrom(in);
    return shardId;
  }

  public LakeHouse getLakeHouse() {
    return lakeHouse;
  }

  public void setLakeHouse(LakeHouse lakeHouse) {
    this.lakeHouse = lakeHouse;
  }

  public int getNodeId() {
    return nodeId;
  }

  public void setNodeId(int nodeId) {
    this.nodeId = nodeId;
  }

  // TODO needs to rewrite.

  // TODO needs to rewrite.
  @Override
  public String toString() {
    return "LakeHouse Node [" + lakeHouse.name() + "][" + nodeId + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LakeHouseNode lakeHouseNode = (LakeHouseNode) o;

    if (nodeId != lakeHouseNode.nodeId) {
      return false;
    }
    return lakeHouse != null ? lakeHouse.equals(lakeHouseNode.lakeHouse)
        : lakeHouseNode.lakeHouse == null;
  }

  @Override
  public int hashCode() {
    int result = lakeHouse != null ? lakeHouse.hashCode() : 0;
    result = 31 * result + nodeId;
    return result;
  }

  /**
   * TODO needs to rewrite，
   */
  @Override
  public void readFrom(StreamInput in) throws IOException {

    //call engine.lakehouse create
    //call engine.lakespace create
    //call engine.laketable create
    nodeId = in.readVInt();
  }

  // TODO needs to rewrite，

  @Override
  public void writeTo(StreamOutput out) throws IOException {
  }
}
