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

package io.hermes.monitor.dump;

/**
 * @author spancer.ray
 */
public class DumpContributionFailedException extends DumpException {

  private final String name;

  public DumpContributionFailedException(String name, String msg) {
    this(name, msg, null);
  }

  public DumpContributionFailedException(String name, String msg, Throwable cause) {
    super(name + ": " + msg, cause);
    this.name = name;
  }

  public String name() {
    return this.name;
  }
}
