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

package io.hermes.monitor.dump.cluster;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.hermes.monitor.dump.Dump;
import io.hermes.monitor.dump.DumpContributionFailedException;
import io.hermes.monitor.dump.DumpContributor;
import io.hermes.util.settings.Settings;
import java.io.PrintWriter;

/**
 * @author spancer.ray
 */
public class ClusterDumpContributor implements DumpContributor {

  public static final String CLUSTER = "cluster";

  private final String name;


  @Inject
  public ClusterDumpContributor(@Assisted String name, @Assisted Settings settings) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void contribute(Dump dump) throws DumpContributionFailedException {

    PrintWriter writer = new PrintWriter(dump.createFileWriter("cluster.txt"));

    writer.close();
  }
}
