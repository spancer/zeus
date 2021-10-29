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

package io.hermes.laketable;

import io.hermes.util.concurrent.Immutable;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;
import java.io.Serializable;

/**
 * 表名，数据湖中的表
 *
 * @author spancer.ray
 */
@Immutable
public class LakeTable implements Serializable, Streamable {

  private String name;

  private LakeTable() {

  }

  public LakeTable(String name) {
    this.name = name;
  }

  public static LakeTable readTableName(StreamInput in) throws IOException {
    LakeTable index = new LakeTable();
    index.readFrom(in);
    return index;
  }

  public String name() {
    return this.name;
  }

  public String getName() {
    return name();
  }

  @Override
  public String toString() {
    return "Table [" + name + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LakeTable table = (LakeTable) o;

    return name != null ? name.equals(table.name) : table.name == null;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    name = in.readUTF();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeUTF(name);
  }
}
