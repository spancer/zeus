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

/**
 * A stack of byte primitives, backed by a TByteArrayList.
 *
 * @author Eric D. Friedman, Rob Eden
 * @version $Id: PStack.template,v 1.2 2007/02/28 23:03:57 robeden Exp $
 */

public class TByteStack {

  public static final int DEFAULT_CAPACITY = TByteArrayList.DEFAULT_CAPACITY;
  /**
   * the list used to hold the stack values.
   */
  protected TByteArrayList _list;

  /**
   * Creates a new <code>TByteStack</code> instance with the default capacity.
   */
  public TByteStack() {
    this(DEFAULT_CAPACITY);
  }

  /**
   * Creates a new <code>TByteStack</code> instance with the specified capacity.
   *
   * @param capacity the initial depth of the stack
   */
  public TByteStack(int capacity) {
    _list = new TByteArrayList(capacity);
  }

  /**
   * Pushes the value onto the top of the stack.
   *
   * @param val an <code>byte</code> value
   */
  public void push(byte val) {
    _list.add(val);
  }

  /**
   * Removes and returns the value at the top of the stack.
   *
   * @return an <code>byte</code> value
   */
  public byte pop() {
    return _list.remove(_list.size() - 1);
  }

  /**
   * Returns the value at the top of the stack.
   *
   * @return an <code>byte</code> value
   */
  public byte peek() {
    return _list.get(_list.size() - 1);
  }

  /**
   * Returns the current depth of the stack.
   */
  public int size() {
    return _list.size();
  }

  /**
   * Clears the stack, reseting its capacity to the default.
   */
  public void clear() {
    _list.clear(DEFAULT_CAPACITY);
  }

  /**
   * Clears the stack without releasing its internal capacity allocation.
   */
  public void reset() {
    _list.reset();
  }

  /**
   * Copies the contents of the stack into a native array. Note that this will NOT pop them out of
   * the stack.
   *
   * @return an <code>byte[]</code> value
   */
  public byte[] toNativeArray() {
    return _list.toNativeArray();
  }

  /**
   * Copies a slice of the list into a native array. Note that this will NOT pop them out of the
   * stack.
   *
   * @param dest the array to copy into.
   */
  public void toNativeArray(byte[] dest) {
    _list.toNativeArray(dest, 0, size());
  }
} // TByteStack
