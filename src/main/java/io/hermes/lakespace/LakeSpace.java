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
/*
 * Copyright (c) Chalco-Steering Technology Co., Ltd. All Rights Reserved. This software is licensed
 * not sold. Use or reproduction of this software by any unauthorized individual or entity is
 * strictly prohibited. This software is the confidential and proprietary information of
 * Chalco-Steering Technology Co., Ltd. Disclosure of such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into with Chalco-Steering
 * Technology Co., Ltd. Chalco-Steering Technology Co., Ltd. MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * Chalco-Steering Technology Co., Ltd. SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ANY DERIVATIVES THEREOF.
 */
package io.hermes.lakespace;

import java.io.IOException;
import java.io.Serializable;
import io.hermes.lakehouse.LakeHouse;
import io.hermes.util.concurrent.Immutable;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;

/**
 * 湖仓空间，可类比为数仓分层，理论上接入数据湖的数据表都在一个默认的lakehouse下的默认的lakespace中。
 *
 * @author spancer.ray
 */
@Immutable
public class LakeSpace implements Serializable, Streamable {

  private String name;

  private LakeHouse lakeHouse;

  private LakeSpace() {

  }

  public LakeSpace(String name) {
    this.name = name;
  }

  public static LakeSpace readSpaceName(StreamInput in) throws IOException {
    LakeSpace index = new LakeSpace();
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
    return "Lake Space [" + name + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LakeSpace lakeSpace = (LakeSpace) o;

    return name != null ? name.equals(lakeSpace.name) : lakeSpace.name == null;
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
