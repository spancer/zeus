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

/**
 * @author spancer.ray
 */
public class Booleans {

  public static boolean parseBoolean(String value, boolean defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    return !(value.equals("false") || value.equals("0") || value.equals("off"));
  }

  public static Boolean parseBoolean(String value, Boolean defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    return !(value.equals("false") || value.equals("0") || value.equals("off"));
  }
}
