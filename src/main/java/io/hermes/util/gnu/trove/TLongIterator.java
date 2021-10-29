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

package io.hermes.util.gnu.trove;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Iterator for long collections.
 *
 * @author Eric D. Friedman
 * @version $Id: PIterator.template,v 1.1 2006/11/10 23:28:00 robeden Exp $
 */

public class TLongIterator extends TPrimitiveIterator {

  /**
   * the collection on which the iterator operates
   */
  private final TLongHash _hash;

  /**
   * Creates a TLongIterator for the elements in the specified collection.
   */
  public TLongIterator(TLongHash hash) {
    super(hash);
    this._hash = hash;
  }

  /**
   * Advances the iterator to the next element in the underlying collection and returns it.
   *
   * @return the next long in the collection
   * @throws NoSuchElementException if the iterator is already exhausted
   */
  public long next() {
    moveToNextIndex();
    return _hash._set[_index];
  }
}// TLongIterator
