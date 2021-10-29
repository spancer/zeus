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

import io.hermes.util.Nullable;
import java.io.File;
import java.util.Map;

/**
 * @author spancer.ray
 */
public interface DumpGenerator {

  Result generateDump(String cause, @Nullable Map<String, Object> context)
      throws DumpGenerationFailedException;

  Result generateDump(String cause, @Nullable Map<String, Object> context, String... contributors)
      throws DumpGenerationFailedException;

  class Result {

    private final File location;
    private Iterable<DumpContributionFailedException> failedContributors;

    public Result(File location, Iterable<DumpContributionFailedException> failedContributors) {
      this.location = location;
      this.failedContributors = failedContributors;
    }

    public String location() {
      return location.toString();
    }

    public Iterable<DumpContributionFailedException> failedContributors() {
      return failedContributors;
    }
  }
}
