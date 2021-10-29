/*******************************************************************************
 * Copyright 2021 spancer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package io.hermes.action.admin.cluster.health;

import static io.hermes.util.TimeValue.readTimeValue;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import io.hermes.action.ActionRequestValidationException;
import io.hermes.action.support.master.MasterNodeOperationRequest;
import io.hermes.util.Strings;
import io.hermes.util.TimeValue;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;

/**
 * @author spancer.ray
 */
public class ClusterHealthRequest extends MasterNodeOperationRequest {

  private String[] lakeHouses;

  private TimeValue timeout = new TimeValue(30, TimeUnit.SECONDS);

  private ClusterHealthStatus waitForStatus;

  private int waitForRelocatingShards = -1;

  ClusterHealthRequest() {}

  public ClusterHealthRequest(String... lakeHouses) {
    this.lakeHouses = lakeHouses;
  }

  public String[] lakeHouses() {
    return lakeHouses;
  }

  public ClusterHealthRequest lakeHouses(String[] lakeHouses) {
    this.lakeHouses = lakeHouses;
    return this;
  }

  public TimeValue timeout() {
    return timeout;
  }

  public ClusterHealthRequest timeout(TimeValue timeout) {
    this.timeout = timeout;
    return this;
  }

  public ClusterHealthStatus waitForStatus() {
    return waitForStatus;
  }

  public ClusterHealthRequest waitForStatus(ClusterHealthStatus waitForStatus) {
    this.waitForStatus = waitForStatus;
    return this;
  }

  public ClusterHealthRequest waitForGreenStatus() {
    return waitForStatus(ClusterHealthStatus.GREEN);
  }

  public ClusterHealthRequest waitForYellowStatus() {
    return waitForStatus(ClusterHealthStatus.YELLOW);
  }

  public int waitForRelocatingShards() {
    return waitForRelocatingShards;
  }

  public ClusterHealthRequest waitForRelocatingShards(int waitForRelocatingShards) {
    this.waitForRelocatingShards = waitForRelocatingShards;
    return this;
  }

  public ActionRequestValidationException validate() {
    return null;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    super.readFrom(in);
    int size = in.readVInt();
    if (size == 0) {
      lakeHouses = Strings.EMPTY_ARRAY;
    } else {
      lakeHouses = new String[size];
      for (int i = 0; i < lakeHouses.length; i++) {
        lakeHouses[i] = in.readUTF();
      }
    }
    timeout = readTimeValue(in);
    if (in.readBoolean()) {
      waitForStatus = ClusterHealthStatus.fromValue(in.readByte());
    }
    waitForRelocatingShards = in.readInt();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    super.writeTo(out);
    if (lakeHouses == null) {
      out.writeVInt(0);
    } else {
      out.writeVInt(lakeHouses.length);
      for (String index : lakeHouses) {
        out.writeUTF(index);
      }
    }
    timeout.writeTo(out);
    if (waitForStatus == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeByte(waitForStatus.value());
    }
    out.writeInt(waitForRelocatingShards);
  }
}
