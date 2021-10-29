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

import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.settings.Settings;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class ClusterName implements Streamable {

  public static final String SETTING = "cluster.name";

  public static final ClusterName DEFAULT = new ClusterName("hermes");

  private String value;

  private ClusterName() {

  }

  public ClusterName(String value) {
    this.value = value;
  }

  public static ClusterName clusterNameFromSettings(Settings settings) {
    return new ClusterName(settings.get("cluster.name", ClusterName.DEFAULT.value()));
  }

  public static ClusterName readClusterName(StreamInput in) throws IOException {
    ClusterName clusterName = new ClusterName();
    clusterName.readFrom(in);
    return clusterName;
  }

  public String value() {
    return this.value;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    value = in.readUTF();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeUTF(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClusterName that = (ClusterName) o;

    return value != null ? value.equals(that.value) : that.value == null;
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Cluster [" + value + "]";
  }
}
