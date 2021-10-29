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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * An open addressed set implementation for double primitives.
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 */

public class TDoubleHashSet extends TDoubleHash implements Externalizable {

  static final long serialVersionUID = 1L;

  /**
   * Creates a new <code>TDoubleHashSet</code> instance with the default capacity and load factor.
   */
  public TDoubleHashSet() {
    super();
  }

  /**
   * Creates a new <code>TDoubleHashSet</code> instance with a prime capacity equal to or greater
   * than <tt>initialCapacity</tt> and with the default load factor.
   *
   * @param initialCapacity an <code>int</code> value
   */
  public TDoubleHashSet(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Creates a new <code>TDoubleHashSet</code> instance with a prime capacity equal to or greater
   * than <tt>initialCapacity</tt> and with the specified load factor.
   *
   * @param initialCapacity an <code>int</code> value
   * @param loadFactor      a <code>float</code> value
   */
  public TDoubleHashSet(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  /**
   * Creates a new <code>TDoubleHashSet</code> instance containing the elements of <tt>array</tt>.
   *
   * @param array an array of <code>double</code> primitives
   */
  public TDoubleHashSet(double[] array) {
    this(array.length);
    addAll(array);
  }

  /**
   * Creates a new <code>TDoubleHash</code> instance with the default capacity and load factor.
   *
   * @param strategy used to compute hash codes and to compare keys.
   */
  public TDoubleHashSet(TDoubleHashingStrategy strategy) {
    super(strategy);
  }

  /**
   * Creates a new <code>TDoubleHash</code> instance whose capacity is the next highest prime above
   * <tt>initialCapacity + 1</tt> unless that value is already prime.
   *
   * @param initialCapacity an <code>int</code> value
   * @param strategy        used to compute hash codes and to compare keys.
   */
  public TDoubleHashSet(int initialCapacity, TDoubleHashingStrategy strategy) {
    super(initialCapacity, strategy);
  }

  /**
   * Creates a new <code>TDoubleHash</code> instance with a prime value at or near the specified
   * capacity and load factor.
   *
   * @param initialCapacity used to find a prime capacity for the table.
   * @param loadFactor      used to calculate the threshold over which rehashing takes place.
   * @param strategy        used to compute hash codes and to compare keys.
   */
  public TDoubleHashSet(int initialCapacity, float loadFactor, TDoubleHashingStrategy strategy) {
    super(initialCapacity, loadFactor, strategy);
  }

  /**
   * Creates a new <code>TDoubleHashSet</code> instance containing the elements of <tt>array</tt>.
   *
   * @param array    an array of <code>double</code> primitives
   * @param strategy used to compute hash codes and to compare keys.
   */
  public TDoubleHashSet(double[] array, TDoubleHashingStrategy strategy) {
    this(array.length, strategy);
    addAll(array);
  }

  /**
   * @return a TDoubleIterator with access to the values in this set
   */
  public TDoubleIterator iterator() {
    return new TDoubleIterator(this);
  }

  /**
   * Inserts a value into the set.
   *
   * @param val an <code>double</code> value
   * @return true if the set was modified by the add operation
   */
  public boolean add(double val) {
    int index = insertionIndex(val);

    if (index < 0) {
      return false; // already present in set, nothing to add
    }

    byte previousState = _states[index];
    _set[index] = val;
    _states[index] = FULL;
    postInsertHook(previousState == FREE);

    return true; // yes, we added something
  }

  /**
   * Expands the set to accommodate new values.
   *
   * @param newCapacity an <code>int</code> value
   */
  protected void rehash(int newCapacity) {
    int oldCapacity = _set.length;
    double[] oldSet = _set;
    byte[] oldStates = _states;

    _set = new double[newCapacity];
    _states = new byte[newCapacity];

    for (int i = oldCapacity; i-- > 0; ) {
      if (oldStates[i] == FULL) {
        double o = oldSet[i];
        int index = insertionIndex(o);
        _set[index] = o;
        _states[index] = FULL;
      }
    }
  }

  /**
   * Returns a new array containing the values in the set.
   *
   * @return an <code>double[]</code> value
   */
  public double[] toArray() {
    double[] result = new double[size()];
    double[] set = _set;
    byte[] states = _states;

    for (int i = states.length, j = 0; i-- > 0; ) {
      if (states[i] == FULL) {
        result[j++] = set[i];
      }
    }
    return result;
  }

  /**
   * Empties the set.
   */
  public void clear() {
    super.clear();
    double[] set = _set;
    byte[] states = _states;

    for (int i = set.length; i-- > 0; ) {
      set[i] = 0;
      states[i] = FREE;
    }
  }

  /**
   * Compares this set with another set for equality of their stored entries.
   *
   * @param other an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object other) {
    if (!(other instanceof TDoubleHashSet)) {
      return false;
    }
    final TDoubleHashSet that = (TDoubleHashSet) other;
    if (that.size() != this.size()) {
      return false;
    }
    return forEach(new TDoubleProcedure() {
      public final boolean execute(double value) {
        return that.contains(value);
      }
    });
  }

  public int hashCode() {
    HashProcedure p = new HashProcedure();
    forEach(p);
    return p.getHashCode();
  }

  /**
   * Removes <tt>val</tt> from the set.
   *
   * @param val an <code>double</code> value
   * @return true if the set was modified by the remove operation.
   */
  public boolean remove(double val) {
    int index = index(val);
    if (index >= 0) {
      removeAt(index);
      return true;
    }
    return false;
  }

  /**
   * Tests the set to determine if all of the elements in <tt>array</tt> are present.
   *
   * @param array an <code>array</code> of double primitives.
   * @return true if all elements were present in the set.
   */
  public boolean containsAll(double[] array) {
    for (int i = array.length; i-- > 0; ) {
      if (!contains(array[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Adds all of the elements in <tt>array</tt> to the set.
   *
   * @param array an <code>array</code> of double primitives.
   * @return true if the set was modified by the add all operation.
   */
  public boolean addAll(double[] array) {
    boolean changed = false;
    for (int i = array.length; i-- > 0; ) {
      if (add(array[i])) {
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Removes all of the elements in <tt>array</tt> from the set.
   *
   * @param array an <code>array</code> of double primitives.
   * @return true if the set was modified by the remove all operation.
   */
  public boolean removeAll(double[] array) {
    boolean changed = false;
    for (int i = array.length; i-- > 0; ) {
      if (remove(array[i])) {
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Removes any values in the set which are not contained in <tt>array</tt>.
   *
   * @param array an <code>array</code> of double primitives.
   * @return true if the set was modified by the retain all operation
   */
  public boolean retainAll(double[] array) {
    boolean changed = false;
    Arrays.sort(array);
    double[] set = _set;
    byte[] states = _states;

    for (int i = set.length; i-- > 0; ) {
      if (states[i] == FULL && (Arrays.binarySearch(array, set[i]) < 0)) {
        remove(set[i]);
        changed = true;
      }
    }
    return changed;
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    // VERSION
    out.writeByte(0);

    // NUMBER OF ENTRIES
    out.writeInt(_size);

    // ENTRIES
    SerializationProcedure writeProcedure = new SerializationProcedure(out);
    if (!forEach(writeProcedure)) {
      throw writeProcedure.exception;
    }
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    // VERSION
    in.readByte();

    // NUMBER OF ENTRIES
    int size = in.readInt();

    // ENTRIES
    setUp(size);
    while (size-- > 0) {
      double val = in.readDouble();
      add(val);
    }
  }

  private final class HashProcedure implements TDoubleProcedure {

    private int h = 0;

    public int getHashCode() {
      return h;
    }

    public final boolean execute(double key) {
      h += _hashingStrategy.computeHashCode(key);
      return true;
    }
  }
} // TDoubleHashSet
