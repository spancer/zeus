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



package io.hermes.action;


import static io.hermes.ExceptionsHelper.detailedMessage;

import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class DefaultLakeHouseNodeOperationFailedException
    implements LakeHouseNodeOperationFailedException {

  private String lakeHouse;

  private int nodeId;

  private String reason;

  private DefaultLakeHouseNodeOperationFailedException() {

  }

  public DefaultLakeHouseNodeOperationFailedException(Throwable e) {
    this.reason = detailedMessage(e);
  }

  public DefaultLakeHouseNodeOperationFailedException(String lakeHouse, int nodeId, Throwable t) {
    this.lakeHouse = lakeHouse;
    this.nodeId = nodeId;
    this.reason = detailedMessage(t);
  }

  public static DefaultLakeHouseNodeOperationFailedException readShardOperationFailed(
      StreamInput in) throws IOException {
    DefaultLakeHouseNodeOperationFailedException exp =
        new DefaultLakeHouseNodeOperationFailedException();
    exp.readFrom(in);
    return exp;
  }

  @Override
  public String lakeHouse() {
    return this.lakeHouse;
  }

  @Override
  public int nodeId() {
    return this.nodeId;
  }

  @Override
  public String reason() {
    return this.reason;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    if (in.readBoolean()) {
      lakeHouse = in.readUTF();
    }
    nodeId = in.readVInt();
    reason = in.readUTF();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    if (lakeHouse == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(lakeHouse);
    }
    out.writeVInt(nodeId);
    out.writeUTF(reason);
  }
}
