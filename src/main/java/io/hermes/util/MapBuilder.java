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

import static com.google.common.collect.Maps.newHashMap;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * @author spancer.ray
 */
public class MapBuilder<K, V> {

  private Map<K, V> map = newHashMap();

  public MapBuilder() {
    this.map = newHashMap();
  }

  public static <K, V> MapBuilder<K, V> newMapBuilder() {
    return new MapBuilder<K, V>();
  }

  public static <K, V> MapBuilder<K, V> newMapBuilder(Map<K, V> map) {
    return new MapBuilder<K, V>().putAll(map);
  }

  public MapBuilder<K, V> putAll(Map<K, V> map) {
    this.map.putAll(map);
    return this;
  }

  public MapBuilder<K, V> put(K key, V value) {
    this.map.put(key, value);
    return this;
  }

  public MapBuilder<K, V> remove(K key) {
    this.map.remove(key);
    return this;
  }

  public V get(K key) {
    return map.get(key);
  }

  public boolean containsKey(K key) {
    return map.containsKey(key);
  }

  public Map<K, V> map() {
    return this.map;
  }

  public ImmutableMap<K, V> immutableMap() {
    return ImmutableMap.copyOf(map);
  }
}
