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

package io.hermes;

/**
 * This class is used to ....
 *
 * @author spancer.ray
 */
public final class ExceptionsHelper {

  /**
   * This method is used to ....
   *
   * @param t param value
   * @return return result.
   */
  public static Throwable unwrapCause(Throwable t) {
    Throwable result = t;
    while (result instanceof HermesWrapperException) {
      result = t.getCause();
    }
    return result;
  }

  public static String detailedMessage(Throwable t) {
    return detailedMessage(t, false, 0);
  }

  public static String detailedMessage(Throwable t, boolean newLines, int initialCounter) {
    if (t == null) {
      return "Unknown";
    }
    int counter = initialCounter + 1;
    if (t.getCause() != null) {
      StringBuilder sb = new StringBuilder();
      while (t != null) {
        if (t.getMessage() != null) {
          sb.append(t.getClass().getSimpleName()).append("[");
          sb.append(t.getMessage());
          sb.append("]");
          if (!newLines) {
            sb.append("; ");
          }
        }
        t = t.getCause();
        if (t != null) {
          if (newLines) {
            sb.append("\n");
            for (int i = 0; i < counter; i++) {
              sb.append("\t");
            }
          } else {
            sb.append("nested: ");
          }
        }
        counter++;
      }
      return sb.toString();
    } else {
      return t.getClass().getSimpleName() + "[" + t.getMessage() + "]";
    }
  }
}
