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

package io.hermes.util.gnu.trove.decorator;

import io.hermes.util.gnu.trove.TIntHashSet;
import io.hermes.util.gnu.trove.TIntIterator;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Wrapper class to make a TIntHashSet conform to the <tt>java.util.Set</tt> API. This class simply
 * decorates an underlying TIntHashSet and translates the Object-based APIs into their Trove
 * primitive analogs.
 * <p/>
 * <p/>
 * Note that wrapping and unwrapping primitive values is extremely inefficient. If possible, users
 * of this class should override the appropriate methods in this class and use a table of canonical
 * values.
 * </p>
 * <p/>
 * Created: Tue Sep 24 22:08:17 PDT 2002
 *
 * @author Eric D. Friedman
 */
public class TIntHashSetDecorator extends AbstractSet<Integer>
    implements Set<Integer>, Externalizable {

  /**
   * the wrapped primitive set
   */
  protected TIntHashSet _set;


  /**
   * FOR EXTERNALIZATION ONLY!!
   */
  public TIntHashSetDecorator() {
  }

  /**
   * Creates a wrapper that decorates the specified primitive set.
   */
  public TIntHashSetDecorator(TIntHashSet set) {
    super();
    this._set = set;
  }


  /**
   * Returns a reference to the set wrapped by this decorator.
   */
  public TIntHashSet getSet() {
    return _set;
  }

  /**
   * Clones the underlying trove collection and returns the clone wrapped in a new decorator
   * instance. This is a shallow clone except where primitives are concerned.
   *
   * @return a copy of the receiver
   */
  public TIntHashSetDecorator clone() {
    try {
      TIntHashSetDecorator copy = (TIntHashSetDecorator) super.clone();
      copy._set = (TIntHashSet) _set.clone();
      return copy;
    } catch (CloneNotSupportedException e) {
      // assert(false);
      throw new InternalError(); // we are cloneable
    }
  }

  /**
   * Inserts a value into the set.
   *
   * @param value true if the set was modified by the insertion
   */
  public boolean add(Integer value) {
    return _set.add(unwrap(value));
  }

  /**
   * Compares this set with another set for equality of their stored entries.
   *
   * @param other an <code>Object</code> value
   * @return true if the sets are identical
   */
  public boolean equals(Object other) {
    if (_set.equals(other)) {
      return true; // comparing two trove sets
    } else if (other instanceof Set) {
      Set that = (Set) other;
      if (that.size() != _set.size()) {
        return false; // different sizes, no need to compare
      } else { // now we have to do it the hard way
        Iterator it = that.iterator();
        for (int i = that.size(); i-- > 0; ) {
          Object val = it.next();
          if (val instanceof Integer) {
            int v = unwrap(val);
            if (_set.contains(v)) {
              // match, ok to continue
            } else {
              return false; // no match: we're done
            }
          } else {
            return false; // different type in other set
          }
        }
        return true; // all entries match
      }
    } else {
      return false;
    }
  }

  /**
   * Empties the set.
   */
  public void clear() {
    this._set.clear();
  }

  /**
   * Deletes a value from the set.
   *
   * @param value an <code>Object</code> value
   * @return true if the set was modified
   */
  public boolean remove(Object value) {
    return _set.remove(unwrap(value));
  }

  /**
   * Creates an iterator over the values of the set.
   *
   * @return an iterator with support for removals in the underlying set
   */
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      private final TIntIterator it = _set.iterator();

      public Integer next() {
        return wrap(it.next());
      }

      public boolean hasNext() {
        return it.hasNext();
      }

      public void remove() {
        it.remove();
      }
    };
  }

  /**
   * Returns the number of entries in the set.
   *
   * @return the set's size.
   */
  public int size() {
    return this._set.size();
  }

  /**
   * Indicates whether set has any entries.
   *
   * @return true if the set is empty
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Wraps a value
   *
   * @param k value in the underlying set
   * @return an Object representation of the value
   */
  protected Integer wrap(int k) {
    return Integer.valueOf(k);
  }

  /**
   * Unwraps a value
   *
   * @param value wrapped value
   * @return an unwrapped representation of the value
   */
  protected int unwrap(Object value) {
    return ((Integer) value).intValue();
  }

  // Implements Externalizable

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    // VERSION
    in.readByte();

    // SET
    _set = (TIntHashSet) in.readObject();
  }

  // Implements Externalizable

  public void writeExternal(ObjectOutput out) throws IOException {
    // VERSION
    out.writeByte(0);

    // SET
    out.writeObject(_set);
  }
} // TIntHashSetDecorator
