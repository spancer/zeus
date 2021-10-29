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

import com.google.inject.AbstractModule;
import io.hermes.cluster.ClusterService;
import io.hermes.cluster.service.InternalClusterService;
import io.hermes.discovery.DiscoveryModule;
import io.hermes.util.logging.Loggers;
import io.hermes.util.settings.NoClassSettingsException;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class TransportClientClusterModule extends AbstractModule {

  private final Settings settings;

  public TransportClientClusterModule(Settings settings) {
    this.settings = settings;
  }

  @Override
  protected void configure() {
    try {
      new DiscoveryModule(settings).configure(binder());
      bind(ClusterService.class).to(InternalClusterService.class).asEagerSingleton();
      bind(TransportClientClusterService.class).asEagerSingleton();
    } catch (NoClassSettingsException e) {
      // that's fine, no actual implementation for discovery
    } catch (Exception e) {
      Loggers.getLogger(getClass(), settings).warn("Failed to load discovery", e);
    }
  }
}
