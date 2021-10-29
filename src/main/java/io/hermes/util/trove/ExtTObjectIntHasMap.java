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

import io.hermes.util.gnu.trove.TObjectHashingStrategy;
import io.hermes.util.gnu.trove.TObjectIntHashMap;

/**
 * @author spancer.ray
 */
public class ExtTObjectIntHasMap<T> extends TObjectIntHashMap<T> {

  private int defaultReturnValue = 0;

  public ExtTObjectIntHasMap() {
  }

  public ExtTObjectIntHasMap(int initialCapacity) {
    super(initialCapacity);
  }

  public ExtTObjectIntHasMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public ExtTObjectIntHasMap(TObjectHashingStrategy<T> ttObjectHashingStrategy) {
    super(ttObjectHashingStrategy);
  }

  public ExtTObjectIntHasMap(int initialCapacity,
      TObjectHashingStrategy<T> ttObjectHashingStrategy) {
    super(initialCapacity, ttObjectHashingStrategy);
  }

  public ExtTObjectIntHasMap(int initialCapacity, float loadFactor,
      TObjectHashingStrategy<T> ttObjectHashingStrategy) {
    super(initialCapacity, loadFactor, ttObjectHashingStrategy);
  }

  public ExtTObjectIntHasMap<T> defaultReturnValue(int defaultReturnValue) {
    this.defaultReturnValue = defaultReturnValue;
    return this;
  }

  @Override
  public int get(T key) {
    int index = index(key);
    return index < 0 ? defaultReturnValue : _values[index];
  }
}
