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



package io.hermes.client.transport;

import com.google.inject.Inject;
import io.hermes.cluster.ClusterService;
import io.hermes.discovery.DiscoveryService;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class TransportClientClusterService extends AbstractComponent {

  private final ClusterService clusterService;

  private final TransportClientNodesService nodesService;

  private final DiscoveryService discoveryService;

  @Inject
  public TransportClientClusterService(Settings settings, ClusterService clusterService,
      TransportClientNodesService nodesService, DiscoveryService discoveryService) {
    super(settings);
    this.clusterService = clusterService;
    this.nodesService = nodesService;
    this.discoveryService = discoveryService;

    clusterService.add(nodesService);
  }

  public void start() {
    clusterService.add(nodesService);
    clusterService.start();
    discoveryService.start();
  }

  public void close() {
    clusterService.remove(nodesService);
    clusterService.close();
    discoveryService.close();
  }
}