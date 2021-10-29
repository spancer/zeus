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

package io.hermes.action;

import io.hermes.HermesException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author spancer.ray
 */
public class ActionRequestValidationException extends HermesException {

  private final List<String> validationErrors = new ArrayList<String>();

  public ActionRequestValidationException() {
    super(null);
  }

  public void addValidationError(String error) {
    validationErrors.add(error);
  }

  public List<String> validationErrors() {
    return validationErrors;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("Validation Failed: ");
    int index = 0;
    for (String error : validationErrors) {
      sb.append(++index).append(": ").append(error);
    }
    return sb.toString();
  }
}
