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

package io.hermes.action.support.broadcast;

import io.hermes.HermesIllegalArgumentException;

/**
 * Controls the operation threading model for broadcast operation that are performed locally on the
 * executing node.
 *
 * @author spancer.ray
 */
public enum BroadcastOperationThreading {
  /**
   * No threads are used, all the local shards operations will be performed on the calling thread.
   */
  NO_THREADS((byte) 0),
  /**
   * The local shards operations will be performed in serial manner on a single forked thread.
   */
  SINGLE_THREAD((byte) 1),
  /**
   * Each local shard operation will execute on its own thread.
   */
  THREAD_PER_SHARD((byte) 2);

  private final byte id;

  BroadcastOperationThreading(byte id) {
    this.id = id;
  }

  public static BroadcastOperationThreading fromId(byte id) {
    if (id == 0) {
      return NO_THREADS;
    }
    if (id == 1) {
      return SINGLE_THREAD;
    }
    if (id == 2) {
      return THREAD_PER_SHARD;
    }
    throw new HermesIllegalArgumentException("No type matching id [" + id + "]");
  }

  public static BroadcastOperationThreading fromString(String value,
      BroadcastOperationThreading defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    return BroadcastOperationThreading.valueOf(value.toUpperCase());
  }

  public byte id() {
    return this.id;
  }
}
