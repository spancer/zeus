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

import io.hermes.HermesIllegalArgumentException;

/**
 * @author spancer.ray
 */
public enum ClusterHealthStatus {
  GREEN((byte) 0),
  YELLOW((byte) 1),
  RED((byte) 2);

  private byte value;

  ClusterHealthStatus(byte value) {
    this.value = value;
  }

  public static ClusterHealthStatus fromValue(byte value) {
    switch (value) {
      case 0:
        return GREEN;
      case 1:
        return YELLOW;
      case 2:
        return RED;
      default:
        throw new HermesIllegalArgumentException(
            "No cluster health status for value [" + value + "]");
    }
  }

  public byte value() {
    return value;
  }
}
