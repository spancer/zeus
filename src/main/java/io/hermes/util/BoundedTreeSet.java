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

package io.hermes.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * @author spancer.ray
 */
public class BoundedTreeSet<E> extends TreeSet<E> {

  private final int size;

  public BoundedTreeSet(int size) {
    this.size = size;
  }

  public BoundedTreeSet(Comparator<? super E> comparator, int size) {
    super(comparator);
    this.size = size;
  }

  @Override
  public boolean add(E e) {
    boolean result = super.add(e);
    rebound();
    return result;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean result = super.addAll(c);
    rebound();
    return result;
  }

  private void rebound() {
    while (size() > size) {
      remove(last());
    }
  }
}
