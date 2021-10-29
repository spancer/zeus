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

package io.hermes.discovery;

import static io.hermes.util.guice.ModulesFactory.createModule;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.hermes.discovery.local.LocalDiscoveryModule;
import io.hermes.discovery.zen.ZenDiscoveryModule;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class DiscoveryModule extends AbstractModule {

  private final Settings settings;

  public DiscoveryModule(Settings settings) {
    this.settings = settings;
  }

  @Override
  protected void configure() {
    Class<? extends Module> defaultDiscoveryModule;
    if (settings.getAsBoolean("node.local", true)) {
      defaultDiscoveryModule = LocalDiscoveryModule.class;
    } else {
      defaultDiscoveryModule = ZenDiscoveryModule.class;
    }

    Class<? extends Module> moduleClass = settings.getAsClass("discovery.type",
        defaultDiscoveryModule, "io.hermes.discovery.", "DiscoveryModule");
    createModule(moduleClass, settings).configure(binder());

    bind(DiscoveryService.class).asEagerSingleton();
  }
}
