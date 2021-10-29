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

import java.io.IOException;
import java.util.Map;

/**
 * @author spancer.ray
 */
public interface ToJson {

  Params EMPTY_PARAMS = new Params() {
    @Override
    public String param(String key) {
      return null;
    }
  };

  void toJson(JsonBuilder builder, Params params) throws IOException;

  interface Params {

    String param(String key);
  }

  class MapParams implements Params {

    private final Map<String, String> params;

    public MapParams(Map<String, String> params) {
      this.params = params;
    }

    @Override
    public String param(String key) {
      return params.get(key);
    }
  }
}
