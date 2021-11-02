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

package io.hermes.util.logging.support;

import java.util.HashMap;
import java.util.Map;

/**
 * @author spancer.ray
 */
public class LoggerMessageFormat {

  static final char DELIM_START = '{';
  static final char DELIM_STOP = '}';
  static final String DELIM_STR = "{}";
  private static final char ESCAPE_CHAR = '\\';

  public static String format(final String messagePattern, final Object... argArray) {
    return format(null, messagePattern, argArray);
  }

  public static String format(final String prefix, final String messagePattern,
      final Object... argArray) {
    if (messagePattern == null) {
      return null;
    }
    if (argArray == null) {
      if (prefix == null) {
        return messagePattern;
      } else {
        return prefix + messagePattern;
      }
    }
    int i = 0;
    int j;
    StringBuffer sbuf = new StringBuffer(messagePattern.length() + 50);
    if (prefix != null) {
      sbuf.append(prefix);
    }

    for (int L = 0; L < argArray.length; L++) {

      j = messagePattern.indexOf(DELIM_STR, i);

      if (j == -1) {
        // no more variables
        if (i == 0) { // this is a simple string
          return messagePattern;
        } else { // add the tail string which contains no variables and return
          // the result.
          sbuf.append(messagePattern.substring(i));
          return sbuf.toString();
        }
      } else {
        if (isEscapedDelimeter(messagePattern, j)) {
          if (!isDoubleEscaped(messagePattern, j)) {
            L--; // DELIM_START was escaped, thus should not be incremented
            sbuf.append(messagePattern, i, j - 1);
            sbuf.append(DELIM_START);
            i = j + 1;
          } else {
            sbuf.append(messagePattern, i, j - 1);
            deeplyAppendParameter(sbuf, argArray[L], new HashMap());
            i = j + 2;
          }
        } else {
          // normal case
          sbuf.append(messagePattern, i, j);
          deeplyAppendParameter(sbuf, argArray[L], new HashMap());
          i = j + 2;
        }
      }
    }
    // append the characters following the last {} pair.
    sbuf.append(messagePattern.substring(i));
    return sbuf.toString();
  }

  static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {

    if (delimeterStartIndex == 0) {
      return false;
    }
    char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
    return potentialEscape == ESCAPE_CHAR;
  }

  static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
    return delimeterStartIndex >= 2
        && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR;
  }

  private static void deeplyAppendParameter(StringBuffer sbuf, Object o, Map seenMap) {
    if (o == null) {
      sbuf.append("null");
      return;
    }
    if (!o.getClass().isArray()) {
      safeObjectAppend(sbuf, o);
    } else {
      // check for primitive array types because they
      // unfortunately cannot be cast to Object[]
      if (o instanceof boolean[]) {
        booleanArrayAppend(sbuf, (boolean[]) o);
      } else if (o instanceof byte[]) {
        byteArrayAppend(sbuf, (byte[]) o);
      } else if (o instanceof char[]) {
        charArrayAppend(sbuf, (char[]) o);
      } else if (o instanceof short[]) {
        shortArrayAppend(sbuf, (short[]) o);
      } else if (o instanceof int[]) {
        intArrayAppend(sbuf, (int[]) o);
      } else if (o instanceof long[]) {
        longArrayAppend(sbuf, (long[]) o);
      } else if (o instanceof float[]) {
        floatArrayAppend(sbuf, (float[]) o);
      } else if (o instanceof double[]) {
        doubleArrayAppend(sbuf, (double[]) o);
      } else {
        objectArrayAppend(sbuf, (Object[]) o, seenMap);
      }
    }
  }

  private static void safeObjectAppend(StringBuffer sbuf, Object o) {
    try {
      String oAsString = o.toString();
      sbuf.append(oAsString);
    } catch (Throwable t) {
      System.err.println("Logger: Failed toString() invocation on an object of type ["
          + o.getClass().getName() + "]");
      t.printStackTrace();
      sbuf.append("[FAILED toString()]");
    }

  }

  private static void objectArrayAppend(StringBuffer sbuf, Object[] a, Map seenMap) {
    sbuf.append('[');
    if (!seenMap.containsKey(a)) {
      seenMap.put(a, null);
      final int len = a.length;
      for (int i = 0; i < len; i++) {
        deeplyAppendParameter(sbuf, a[i], seenMap);
        if (i != len - 1) {
          sbuf.append(", ");
        }
      }
      // allow repeats in siblings
      seenMap.remove(a);
    } else {
      sbuf.append("...");
    }
    sbuf.append(']');
  }

  private static void booleanArrayAppend(StringBuffer sbuf, boolean[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }

  private static void byteArrayAppend(StringBuffer sbuf, byte[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }

  private static void charArrayAppend(StringBuffer sbuf, char[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }

  private static void shortArrayAppend(StringBuffer sbuf, short[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }

  private static void intArrayAppend(StringBuffer sbuf, int[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }

  private static void longArrayAppend(StringBuffer sbuf, long[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }

  private static void floatArrayAppend(StringBuffer sbuf, float[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }

  private static void doubleArrayAppend(StringBuffer sbuf, double[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1) {
        sbuf.append(", ");
      }
    }
    sbuf.append(']');
  }
}
