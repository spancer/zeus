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



package io.hermes.util.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author spancer.ray
 */
public class JacksonNodes {

  public static float nodeFloatValue(JsonNode node) {
    if (node.isNumber()) {
      return node.floatValue();
    }
    String value = node.textValue();
    return Float.parseFloat(value);
  }

  public static double nodeDoubleValue(JsonNode node) {
    if (node.isNumber()) {
      return node.doubleValue();
    }
    String value = node.textValue();
    return Double.parseDouble(value);
  }

  public static int nodeIntegerValue(JsonNode node) {
    if (node.isNumber()) {
      return node.intValue();
    }
    String value = node.textValue();
    return Integer.parseInt(value);
  }

  public static short nodeShortValue(JsonNode node) {
    if (node.isNumber()) {
      return node.shortValue();
    }
    String value = node.textValue();
    return Short.parseShort(value);
  }

  public static long nodeLongValue(JsonNode node) {
    if (node.isNumber()) {
      return node.longValue();
    }
    String value = node.textValue();
    return Long.parseLong(value);
  }

  public static boolean nodeBooleanValue(JsonNode node) {
    if (node.isBoolean()) {
      return node.booleanValue();
    }
    if (node.isNumber()) {
      return node.intValue() != 0;
    }
    String value = node.textValue();
    return !(value.equals("false") || value.equals("0") || value.equals("off"));
  }
}
