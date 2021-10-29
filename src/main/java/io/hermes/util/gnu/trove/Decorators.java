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


import io.hermes.util.gnu.trove.decorator.TByteByteHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TByteDoubleHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TByteFloatHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TByteHashSetDecorator;
import io.hermes.util.gnu.trove.decorator.TByteIntHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TByteLongHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TByteObjectHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TByteShortHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleByteHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleDoubleHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleFloatHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleHashSetDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleIntHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleLongHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleObjectHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TDoubleShortHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatByteHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatDoubleHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatFloatHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatHashSetDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatIntHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatLongHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatObjectHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TFloatShortHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TIntByteHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TIntDoubleHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TIntFloatHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TIntHashSetDecorator;
import io.hermes.util.gnu.trove.decorator.TIntIntHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TIntLongHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TIntObjectHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TIntShortHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TLongByteHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TLongDoubleHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TLongFloatHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TLongHashSetDecorator;
import io.hermes.util.gnu.trove.decorator.TLongIntHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TLongLongHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TLongObjectHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TLongShortHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TObjectByteHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TObjectDoubleHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TObjectFloatHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TObjectIntHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TObjectLongHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TObjectShortHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TShortByteHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TShortDoubleHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TShortFloatHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TShortHashSetDecorator;
import io.hermes.util.gnu.trove.decorator.TShortIntHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TShortLongHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TShortObjectHashMapDecorator;
import io.hermes.util.gnu.trove.decorator.TShortShortHashMapDecorator;
import java.util.Map;
import java.util.Set;


/**
 * This is a static utility class that provides functions for simplifying creation of decorators.
 *
 * @author Robert D. Eden
 * @since Trove 2.1
 */
public class Decorators {
  // Hide the constructor

  private Decorators() {
  }


  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Double, Double> wrap(TDoubleDoubleHashMap map) {
    return new TDoubleDoubleHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Double, Float> wrap(TDoubleFloatHashMap map) {
    return new TDoubleFloatHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Double, Integer> wrap(TDoubleIntHashMap map) {
    return new TDoubleIntHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Double, Long> wrap(TDoubleLongHashMap map) {
    return new TDoubleLongHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Double, Byte> wrap(TDoubleByteHashMap map) {
    return new TDoubleByteHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Double, Short> wrap(TDoubleShortHashMap map) {
    return new TDoubleShortHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Float, Double> wrap(TFloatDoubleHashMap map) {
    return new TFloatDoubleHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Float, Float> wrap(TFloatFloatHashMap map) {
    return new TFloatFloatHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Float, Integer> wrap(TFloatIntHashMap map) {
    return new TFloatIntHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Float, Long> wrap(TFloatLongHashMap map) {
    return new TFloatLongHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Float, Byte> wrap(TFloatByteHashMap map) {
    return new TFloatByteHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Float, Short> wrap(TFloatShortHashMap map) {
    return new TFloatShortHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Integer, Double> wrap(TIntDoubleHashMap map) {
    return new TIntDoubleHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Integer, Float> wrap(TIntFloatHashMap map) {
    return new TIntFloatHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Integer, Integer> wrap(TIntIntHashMap map) {
    return new TIntIntHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Integer, Long> wrap(TIntLongHashMap map) {
    return new TIntLongHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Integer, Byte> wrap(TIntByteHashMap map) {
    return new TIntByteHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Integer, Short> wrap(TIntShortHashMap map) {
    return new TIntShortHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Long, Double> wrap(TLongDoubleHashMap map) {
    return new TLongDoubleHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Long, Float> wrap(TLongFloatHashMap map) {
    return new TLongFloatHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Long, Integer> wrap(TLongIntHashMap map) {
    return new TLongIntHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Long, Long> wrap(TLongLongHashMap map) {
    return new TLongLongHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Long, Byte> wrap(TLongByteHashMap map) {
    return new TLongByteHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Long, Short> wrap(TLongShortHashMap map) {
    return new TLongShortHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Byte, Double> wrap(TByteDoubleHashMap map) {
    return new TByteDoubleHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Byte, Float> wrap(TByteFloatHashMap map) {
    return new TByteFloatHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Byte, Integer> wrap(TByteIntHashMap map) {
    return new TByteIntHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Byte, Long> wrap(TByteLongHashMap map) {
    return new TByteLongHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Byte, Byte> wrap(TByteByteHashMap map) {
    return new TByteByteHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Byte, Short> wrap(TByteShortHashMap map) {
    return new TByteShortHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Short, Double> wrap(TShortDoubleHashMap map) {
    return new TShortDoubleHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Short, Float> wrap(TShortFloatHashMap map) {
    return new TShortFloatHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Short, Integer> wrap(TShortIntHashMap map) {
    return new TShortIntHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Short, Long> wrap(TShortLongHashMap map) {
    return new TShortLongHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Short, Byte> wrap(TShortByteHashMap map) {
    return new TShortByteHashMapDecorator(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static Map<Short, Short> wrap(TShortShortHashMap map) {
    return new TShortShortHashMapDecorator(map);
  }


  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<T, Double> wrap(TObjectDoubleHashMap<T> map) {
    return new TObjectDoubleHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<T, Float> wrap(TObjectFloatHashMap<T> map) {
    return new TObjectFloatHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<T, Integer> wrap(TObjectIntHashMap<T> map) {
    return new TObjectIntHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<T, Long> wrap(TObjectLongHashMap<T> map) {
    return new TObjectLongHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<T, Byte> wrap(TObjectByteHashMap<T> map) {
    return new TObjectByteHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<T, Short> wrap(TObjectShortHashMap<T> map) {
    return new TObjectShortHashMapDecorator<T>(map);
  }


  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<Double, T> wrap(TDoubleObjectHashMap<T> map) {
    return new TDoubleObjectHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<Float, T> wrap(TFloatObjectHashMap<T> map) {
    return new TFloatObjectHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<Integer, T> wrap(TIntObjectHashMap<T> map) {
    return new TIntObjectHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<Long, T> wrap(TLongObjectHashMap<T> map) {
    return new TLongObjectHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<Byte, T> wrap(TByteObjectHashMap<T> map) {
    return new TByteObjectHashMapDecorator<T>(map);
  }

  /**
   * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map} interface.
   */
  public static <T> Map<Short, T> wrap(TShortObjectHashMap<T> map) {
    return new TShortObjectHashMapDecorator<T>(map);
  }


  /**
   * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set} interface.
   */
  public static Set<Double> wrap(TDoubleHashSet set) {
    return new TDoubleHashSetDecorator(set);
  }

  /**
   * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set} interface.
   */
  public static Set<Float> wrap(TFloatHashSet set) {
    return new TFloatHashSetDecorator(set);
  }

  /**
   * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set} interface.
   */
  public static Set<Integer> wrap(TIntHashSet set) {
    return new TIntHashSetDecorator(set);
  }

  /**
   * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set} interface.
   */
  public static Set<Long> wrap(TLongHashSet set) {
    return new TLongHashSetDecorator(set);
  }

  /**
   * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set} interface.
   */
  public static Set<Byte> wrap(TByteHashSet set) {
    return new TByteHashSetDecorator(set);
  }

  /**
   * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set} interface.
   */
  public static Set<Short> wrap(TShortHashSet set) {
    return new TShortHashSetDecorator(set);
  }
}
