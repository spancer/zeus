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


package io.hermes.cluster.metadata;

import com.google.inject.Inject;
import io.hermes.cluster.ClusterService;
import io.hermes.env.Environment;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class MetaDataService extends AbstractComponent {

  private final Environment environment;

  private final ClusterService clusterService;

  // TODO needs to add LakeHouse and LakeTable metadata actions here.

  @Inject
  public MetaDataService(Settings settings, Environment environment,
      ClusterService clusterService) {
    super(settings);
    this.environment = environment;
    this.clusterService = clusterService;
  }


}
