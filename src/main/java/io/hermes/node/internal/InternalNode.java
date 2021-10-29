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

package io.hermes.node.internal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.hermes.HermesException;
import io.hermes.Version;
import io.hermes.action.TransportActionModule;
import io.hermes.client.Client;
import io.hermes.client.node.NodeClient;
import io.hermes.client.node.NodeClientModule;
import io.hermes.cluster.ClusterModule;
import io.hermes.cluster.ClusterNameModule;
import io.hermes.cluster.ClusterService;
import io.hermes.discovery.DiscoveryModule;
import io.hermes.discovery.DiscoveryService;
import io.hermes.env.Environment;
import io.hermes.env.EnvironmentModule;
import io.hermes.monitor.jvm.JvmConfig;
import io.hermes.node.Node;
import io.hermes.threadpool.ThreadPool;
import io.hermes.threadpool.ThreadPoolModule;
import io.hermes.transport.TransportModule;
import io.hermes.transport.TransportService;
import io.hermes.util.ThreadLocals;
import io.hermes.util.Tuple;
import io.hermes.util.component.Lifecycle;
import io.hermes.util.guice.Injectors;
import io.hermes.util.logging.HermesLogger;
import io.hermes.util.logging.Loggers;
import io.hermes.util.settings.ImmutableSettings.Builder;
import io.hermes.util.settings.Settings;
import io.hermes.util.settings.SettingsModule;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author spancer.ray
 */
public final class InternalNode implements Node {

  private final Lifecycle lifecycle = new Lifecycle();

  private final Injector injector;

  private final Settings settings;

  private final Environment environment;


  private final Client client;

  public InternalNode() throws HermesException {
    this(Builder.EMPTY_SETTINGS, true);
  }

  /**
   * @param pSettings
   * @param loadConfigSettings
   * @throws HermesException
   */
  public InternalNode(Settings pSettings, boolean loadConfigSettings) throws HermesException {
    Tuple<Settings, Environment> tuple =
        InternalSettingsPerparer.prepareSettings(pSettings, loadConfigSettings);

    HermesLogger logger = Loggers.getLogger(Node.class, tuple.v1().get("name"));
    logger.info("{{}}[{}]: Initializing ...", Version.full(), JvmConfig.jvmConfig().pid());

    this.settings = tuple.v1();
    this.environment = tuple.v2();

    ArrayList<Module> modules = new ArrayList<Module>();
    modules.add(new NodeModule(this));
    modules.add(new EnvironmentModule(environment));
    modules.add(new ClusterNameModule(settings));
    modules.add(new SettingsModule(settings));
    modules.add(new ThreadPoolModule(settings));
    modules.add(new TransportModule(settings));
    modules.add(new DiscoveryModule(settings));
    modules.add(new ClusterModule(settings));
    modules.add(new NodeClientModule());
    modules.add(new TransportActionModule());

    injector = Guice.createInjector(modules);

    client = injector.getInstance(NodeClient.class);

    logger.info("{{}}[{}]: Initialized", Version.full(), JvmConfig.jvmConfig().pid());
  }

  public static void main(String[] args) throws Exception {
    final InternalNode node = new InternalNode();
    node.start();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        node.close();
      }
    });

  }

  @Override
  public Settings settings() {
    return this.settings;
  }

  @Override
  public Client client() {
    return client;
  }

  public Node start() {
    if (!lifecycle.moveToStarted()) {
      return this;
    }

    HermesLogger logger = Loggers.getLogger(Node.class, settings.get("name"));
    logger.info("{{}}[{}]: Starting ...", Version.full(), JvmConfig.jvmConfig().pid());

    injector.getInstance(ClusterService.class).start();
    injector.getInstance(TransportService.class).start();
    injector.getInstance(DiscoveryService.class).start();
    logger.info("{{}}[{}]: Started", Version.full(), JvmConfig.jvmConfig().pid());
    return this;
  }

  @Override
  public Node stop() {
    if (!lifecycle.moveToStopped()) {
      return this;
    }
    HermesLogger logger = Loggers.getLogger(Node.class, settings.get("name"));
    logger.info("{{}}[{}]: Stopping ...", Version.full(), JvmConfig.jvmConfig().pid());

    injector.getInstance(ClusterService.class).stop();
    injector.getInstance(DiscoveryService.class).stop();
    injector.getInstance(TransportService.class).stop();

    Injectors.close(injector);

    logger.info("{{}}[{}]: Stopped", Version.full(), JvmConfig.jvmConfig().pid());

    return this;
  }

  public void close() {
    if (lifecycle.started()) {
      stop();
    }
    if (!lifecycle.moveToClosed()) {
      return;
    }

    HermesLogger logger = Loggers.getLogger(Node.class, settings.get("name"));
    logger.info("{{}}[{}]: Closing ...", Version.full(), JvmConfig.jvmConfig().pid());

    injector.getInstance(Client.class).close();
    injector.getInstance(ClusterService.class).close();
    injector.getInstance(DiscoveryService.class).close();
    injector.getInstance(TransportService.class).close();
    injector.getInstance(ThreadPool.class).shutdown();
    try {
      injector.getInstance(ThreadPool.class).awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // ignore
    }
    try {
      injector.getInstance(ThreadPool.class).shutdownNow();
    } catch (Exception e) {
      // ignore
    }

    ThreadLocals.clearReferencesThreadLocals();

    logger.info("{{}}[{}]: Closed", Version.full(), JvmConfig.jvmConfig().pid());
  }

  public Injector injector() {
    return this.injector;
  }
}
