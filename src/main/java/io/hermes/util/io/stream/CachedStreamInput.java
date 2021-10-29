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


package io.hermes.util.io.stream;

import java.lang.ref.SoftReference;

/**
 *
 */
public class CachedStreamInput {

  private static final ThreadLocal<SoftReference<Entry>> cache =
      new ThreadLocal<SoftReference<Entry>>();

  static Entry instance() {
    SoftReference<Entry> ref = cache.get();
    Entry entry = ref == null ? null : ref.get();
    if (entry == null) {
      HandlesStreamInput handles = new HandlesStreamInput();
      entry = new Entry(handles);
      cache.set(new SoftReference<Entry>(entry));
    }
    return entry;
  }

  public static void clear() {
    cache.remove();
  }

  public static HandlesStreamInput cachedHandles(StreamInput in) {
    HandlesStreamInput handles = instance().handles;
    handles.reset(in);
    return handles;
  }

  public static char[] getCharArray(int size) {
    Entry entry = instance();
    if (entry.chars.length < size) {
      entry.chars = new char[size];
    }
    return entry.chars;
  }

  static class Entry {

    final HandlesStreamInput handles;
    char[] chars = new char[80];

    Entry(HandlesStreamInput handles) {
      this.handles = handles;
    }
  }
}
