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

import io.hermes.util.io.stream.Streamable;
import java.io.Serializable;

/**
 * An exception indicating that a failure occurred performing an operation on the LakeHouseNode.
 *
 * @author spancer.ray
 */
public interface LakeHouseNodeOperationFailedException extends Streamable, Serializable {

  /**
   * The lakeHouse the operation failed on. Might return <tt>null</tt> if it can't be derived.
   */
  String lakeHouse();

  /**
   * The lakeHouse the operation failed on. Might return <tt>-1</tt> if it can't be derived.
   */
  int nodeId();

  /**
   * The reason of the failure.
   */
  String reason();
}
