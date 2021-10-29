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

package io.hermes.util.trove;

import io.hermes.util.gnu.trove.TIntArrayList;

/**
 * @author spancer.ray
 */
public class ExtTIntArrayList extends TIntArrayList {

  public ExtTIntArrayList() {
  }

  public ExtTIntArrayList(int capacity) {
    super(capacity);
  }

  public ExtTIntArrayList(int[] values) {
    super(values);
  }

  public int[] unsafeArray() {
    return _data;
  }
}
