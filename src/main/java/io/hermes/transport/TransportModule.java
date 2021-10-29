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

package io.hermes.transport;

import static io.hermes.util.guice.ModulesFactory.createModule;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.hermes.transport.local.LocalTransportModule;
import io.hermes.util.Classes;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class TransportModule extends AbstractModule {

  private final Settings settings;

  public TransportModule(Settings settings) {
    this.settings = settings;
  }

  @Override
  protected void configure() {
    bind(TransportService.class).asEagerSingleton();
    Class<? extends Module> defaultTransportModule;
    if (settings.getAsBoolean("node.local", false)) {
      defaultTransportModule = LocalTransportModule.class;
    } else {
      try {
        Classes.getDefaultClassLoader().loadClass("io.hermes.transport.netty.NettyTransport");
        defaultTransportModule = (Class<? extends Module>) Classes.getDefaultClassLoader()
            .loadClass("io.hermes.transport.netty.NettyTransportModule");
      } catch (ClassNotFoundException e) {
        defaultTransportModule = LocalTransportModule.class;
      }
    }

    Class<? extends Module> moduleClass = settings.getAsClass("transport.type",
        defaultTransportModule, "io.hermes.transport.", "TransportModule");
    createModule(moduleClass, settings).configure(binder());
  }
}
