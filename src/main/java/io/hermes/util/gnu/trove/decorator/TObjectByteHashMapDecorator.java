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

import io.hermes.util.gnu.trove.TObjectByteHashMap;
import io.hermes.util.gnu.trove.TObjectByteIterator;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Wrapper class to make a TObjectByteHashMap conform to the <tt>java.util.Map</tt> API. This class
 * simply decorates an underlying TObjectByteHashMap and translates the Object-based APIs into their
 * Trove primitive analogs.
 * <p/>
 * <p/>
 * Note that wrapping and unwrapping primitive values is extremely inefficient. If possible, users
 * of this class should override the appropriate methods in this class and use a table of canonical
 * values.
 * </p>
 * <p/>
 * Created: Mon Sep 23 22:07:40 PDT 2002
 *
 * @author Eric D. Friedman
 */
public class TObjectByteHashMapDecorator<V> extends AbstractMap<V, Byte>
    implements Map<V, Byte>, Externalizable, Cloneable {

  /**
   * the wrapped primitive map
   */
  protected TObjectByteHashMap<V> _map;


  /**
   * FOR EXTERNALIZATION ONLY!!
   */
  public TObjectByteHashMapDecorator() {
  }


  /**
   * Creates a wrapper that decorates the specified primitive map.
   */
  public TObjectByteHashMapDecorator(TObjectByteHashMap<V> map) {
    super();
    this._map = map;
  }


  /**
   * Returns a reference to the map wrapped by this decorator.
   */
  public TObjectByteHashMap<V> getMap() {
    return _map;
  }


  /**
   * Clones the underlying trove collection and returns the clone wrapped in a new decorator
   * instance. This is a shallow clone except where primitives are concerned.
   *
   * @return a copy of the receiver
   */
  public TObjectByteHashMapDecorator clone() {
    try {
      TObjectByteHashMapDecorator copy = (TObjectByteHashMapDecorator) super.clone();
      copy._map = _map.clone();
      return copy;
    } catch (CloneNotSupportedException e) {
      // assert(false);
      throw new InternalError(); // we are cloneable, so this does not happen
    }
  }

  /**
   * Inserts a key/value pair into the map.
   *
   * @param key   an <code>Object</code> value
   * @param value an <code>Object</code> value
   * @return the previous value associated with <tt>key</tt>, or Integer(0) if none was found.
   */
  public Byte put(V key, Byte value) {
    return wrapValue(_map.put(unwrapKey(key), unwrapValue(value)));
  }

  /**
   * Retrieves the value for <tt>key</tt>
   *
   * @param key an <code>Object</code> value
   * @return the value of <tt>key</tt> or null if no such mapping exists.
   */
  public Byte get(Object key) {
    V k = unwrapKey(key);
    byte v = _map.get(k);
    // 0 may be a false positive since primitive maps
    // cannot return null, so we have to do an extra
    // check here.
    if (v == 0) {
      return _map.containsKey(k) ? wrapValue(v) : null;
    } else {
      return wrapValue(v);
    }
  }


  /**
   * Empties the map.
   */
  public void clear() {
    this._map.clear();
  }

  /**
   * Deletes a key/value pair from the map.
   *
   * @param key an <code>Object</code> value
   * @return the removed value, or Integer(0) if it was not found in the map
   */
  public Byte remove(Object key) {
    return wrapValue(_map.remove(unwrapKey(key)));
  }

  /**
   * Returns a Set view on the entries of the map.
   *
   * @return a <code>Set</code> value
   */
  public Set<Map.Entry<V, Byte>> entrySet() {
    return new AbstractSet<Map.Entry<V, Byte>>() {
      public int size() {
        return _map.size();
      }

      public boolean isEmpty() {
        return TObjectByteHashMapDecorator.this.isEmpty();
      }

      public boolean contains(Object o) {
        if (o instanceof Map.Entry) {
          Object k = ((Map.Entry) o).getKey();
          Object v = ((Map.Entry) o).getValue();
          return TObjectByteHashMapDecorator.this.containsKey(k)
              && TObjectByteHashMapDecorator.this.get(k).equals(v);
        } else {
          return false;
        }
      }

      public Iterator<Map.Entry<V, Byte>> iterator() {
        return new Iterator<Map.Entry<V, Byte>>() {
          private final TObjectByteIterator<V> it = _map.iterator();

          public Map.Entry<V, Byte> next() {
            it.advance();
            final V key = wrapKey(it.key());
            final Byte v = wrapValue(it.value());
            return new Map.Entry<V, Byte>() {
              private Byte val = v;

              public boolean equals(Object o) {
                return o instanceof Map.Entry && ((Map.Entry) o).getKey().equals(key)
                    && ((Map.Entry) o).getValue().equals(val);
              }

              public V getKey() {
                return key;
              }

              public Byte getValue() {
                return val;
              }

              public int hashCode() {
                return key.hashCode() + val.hashCode();
              }

              public Byte setValue(Byte value) {
                val = value;
                return put(key, value);
              }
            };
          }

          public boolean hasNext() {
            return it.hasNext();
          }

          public void remove() {
            it.remove();
          }
        };
      }

      public boolean add(Byte o) {
        throw new UnsupportedOperationException();
      }

      public boolean remove(Object o) {
        throw new UnsupportedOperationException();
      }

      public boolean addAll(Collection<? extends Map.Entry<V, Byte>> c) {
        throw new UnsupportedOperationException();
      }

      public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
      }

      public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
      }

      public void clear() {
        TObjectByteHashMapDecorator.this.clear();
      }
    };
  }

  /**
   * Checks for the presence of <tt>val</tt> in the values of the map.
   *
   * @param val an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean containsValue(Object val) {
    return _map.containsValue(unwrapValue(val));
  }

  /**
   * Checks for the present of <tt>key</tt> in the keys of the map.
   *
   * @param key an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean containsKey(Object key) {
    return _map.containsKey(unwrapKey(key));
  }

  /**
   * Returns the number of entries in the map.
   *
   * @return the map's size.
   */
  public int size() {
    return this._map.size();
  }

  /**
   * Indicates whether map has any entries.
   *
   * @return true if the map is empty
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Copies the key/value mappings in <tt>map</tt> into this map. Note that this will be a
   * <b>deep</b> copy, as storage is by primitive value.
   *
   * @param map a <code>Map</code> value
   */
  public void putAll(Map<? extends V, ? extends Byte> map) {
    Iterator<? extends Entry<? extends V, ? extends Byte>> it = map.entrySet().iterator();
    for (int i = map.size(); i-- > 0; ) {
      Entry<? extends V, ? extends Byte> e = it.next();
      this.put(e.getKey(), e.getValue());
    }
  }

  /**
   * Wraps a key
   *
   * @param o key in the underlying map
   * @return an Object representation of the key
   */
  protected final V wrapKey(Object o) {
    return (V) o;
  }

  /**
   * Unwraps a key
   *
   * @param key wrapped key
   * @return an unwrapped representation of the key
   */
  protected final V unwrapKey(Object key) {
    return (V) key;
  }

  /**
   * Wraps a value
   *
   * @param k value in the underlying map
   * @return an Object representation of the value
   */
  protected Byte wrapValue(byte k) {
    return Byte.valueOf(k);
  }

  /**
   * Unwraps a value
   *
   * @param value wrapped value
   * @return an unwrapped representation of the value
   */
  protected byte unwrapValue(Object value) {
    return ((Byte) value).byteValue();
  }

  // Implements Externalizable

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    // VERSION
    in.readByte();

    // MAP
    _map = (TObjectByteHashMap<V>) in.readObject();
  }

  // Implements Externalizable

  public void writeExternal(ObjectOutput out) throws IOException {
    // VERSION
    out.writeByte(0);

    // MAP
    out.writeObject(_map);
  }

} // TObjectByteHashMapDecorator
