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
 * The same as {@link java.util.concurrent.TimeoutException} simply a runtime one.
 *
 * @author spancer.ray
 */
public class HermesTimeoutException extends HermesException {

  public HermesTimeoutException(String message) {
    super(message);
  }

  public HermesTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
