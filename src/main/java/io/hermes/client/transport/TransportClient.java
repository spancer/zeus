/*******************************************************************************
 * Copyright 2021 spancer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/*
 * Copyright (c) Chalco-Steering Technology Co., Ltd. All Rights Reserved. This software is licensed
 * not sold. Use or reproduction of this software by any unauthorized individual or entity is
 * strictly prohibited. This software is the confidential and proprietary information of
 * Chalco-Steering Technology Co., Ltd. Disclosure of such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into with Chalco-Steering
 * Technology Co., Ltd. Chalco-Steering Technology Co., Ltd. MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * Chalco-Steering Technology Co., Ltd. SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ANY DERIVATIVES THEREOF.
 */


package io.hermes.client.transport;

import static io.hermes.util.settings.ImmutableSettings.settingsBuilder;
import java.util.ArrayList;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.hermes.HermesException;
import io.hermes.HermesIllegalArgumentException;
import io.hermes.client.internal.InternalClient;
import io.hermes.client.transport.support.InternalTransportClient;
import io.hermes.cluster.ClusterNameModule;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.env.Environment;
import io.hermes.env.EnvironmentModule;
import io.hermes.node.internal.InternalSettingsPerparer;
import io.hermes.threadpool.ThreadPool;
import io.hermes.threadpool.ThreadPoolModule;
import io.hermes.timer.TimerModule;
import io.hermes.timer.TimerService;
import io.hermes.transport.TransportModule;
import io.hermes.transport.TransportService;
import io.hermes.util.ThreadLocals;
import io.hermes.util.Tuple;
import io.hermes.util.settings.ImmutableSettings;
import io.hermes.util.settings.Settings;
import io.hermes.util.settings.SettingsModule;
import io.hermes.util.transport.TransportAddress;

/**
 * The transport client allows to create a client that is not part of the cluster, but simply
 * connects to one or more nodes directly by adding their respective addresses using
 * {@link #addTransportAddress(io.hermes.util.transport.TransportAddress)}.
 *
 * <p>
 * The transport client important modules used is the {@link io.hermes.transport.TransportModule}
 * which is started in client mode (only connects, no bind).
 *
 * @author spancer.ray
 */
public class TransportClient implements InternalClient {

  private final Injector injector;

  private final Settings settings;

  private final Environment environment;

  private final TransportClientNodesService nodesService;

  private final InternalTransportClient internalClient;

  /**
   * Constructs a new transport client with settings loaded either from the classpath or the file
   * system (the <tt>hermes.(yml|json)</tt> files optionally prefixed with <tt>config/</tt>).
   */
  public TransportClient() throws HermesException {
    this(ImmutableSettings.Builder.EMPTY_SETTINGS, true);
  }

  /**
   * Constructs a new transport client with explicit settings and settings loaded either from the
   * classpath or the file system (the <tt>hermes.(yml|json)</tt> files optionally prefixed with
   * <tt>config/</tt>).
   */
  public TransportClient(Settings settings) {
    this(settings, true);
  }

  /**
   * Constructs a new transport client with the provided settings and the ability to control if
   * settings will be loaded from the classpath / file system (the <tt>hermes.(yml|json)</tt> files
   * optionally prefixed with <tt>config/</tt>).
   *
   * @param pSettings The explicit settings.
   * @param loadConfigSettings <tt>true</tt> if settings should be loaded from the classpath/file
   *        system.
   * @throws HermesException
   */
  public TransportClient(Settings pSettings, boolean loadConfigSettings) throws HermesException {
    Tuple<Settings, Environment> tuple =
        InternalSettingsPerparer.prepareSettings(pSettings, loadConfigSettings);
    this.settings = settingsBuilder().put(tuple.v1()).put("network.server", false)
        .put("node.client", true).build();
    this.environment = tuple.v2();

    ArrayList<Module> modules = new ArrayList<Module>();
    modules.add(new EnvironmentModule(environment));
    modules.add(new SettingsModule(settings));
    modules.add(new ClusterNameModule(settings));
    modules.add(new TimerModule());
    modules.add(new ThreadPoolModule(settings));

    modules.add(new TransportModule(settings));
    modules.add(new ClientTransportModule());

    if (settings.getAsBoolean("discovery.enabled", true)) {
      modules.add(new TransportClientClusterModule(settings));
    }

    injector = Guice.createInjector(modules);

    injector.getInstance(TransportService.class).start();
    try {
      injector.getInstance(TransportClientClusterService.class).start();
    } catch (Exception e) {
      // ignore
    }

    nodesService = injector.getInstance(TransportClientNodesService.class);
    internalClient = injector.getInstance(InternalTransportClient.class);
  }

  /**
   * Returns the current registered transport addresses to use (added using {@link
   */
  public ImmutableList<TransportAddress> transportAddresses() {
    return nodesService.transportAddresses();
  }

  /**
   * Returns the current connected transport nodes that this client will use.
   *
   * <p>
   * The nodes include all the nodes that are currently alive based on the transport addresses
   * provided.
   */
  public ImmutableList<DiscoveryNode> connectedNodes() {
    return nodesService.connectedNodes();
  }

  /**
   * Adds a transport address that will be used to connect to.
   *
   * <p>
   * The Node this transport address represents will be used if its possible to connect to it. If it
   * is unavailable, it will be automatically connected to once it is up.
   *
   * <p>
   * In order to get the list of all the current connected nodes, please see
   * {@link #connectedNodes()}.
   */
  public TransportClient addTransportAddress(TransportAddress transportAddress) {
    nodesService.addTransportAddress(transportAddress);
    return this;
  }

  /**
   * 该版本不要使用该方法，核心逻辑未实现 Adds a list of transport addresses that will be used to connect to.
   * <p/>
   * <p>
   * The Node this transport address represents will be used if its possible to connect to it. If it
   * is unavailable, it will be automatically connected to once it is up.
   * <p/>
   * <p>
   * In order to get the list of all the current connected nodes, please see
   * {@link #connectedNodes()}.
   */
  @Deprecated
  public TransportClient addTransportAddresses(TransportAddress... transportAddress) {
    throw new HermesIllegalArgumentException("hermes-client此功能暂未支持。");
    // nodesService.addTransportAddresses(transportAddress);
    // return this;
  }

  /**
   * Removes a transport address from the list of transport addresses that are used to connect to.
   */
  public TransportClient removeTransportAddress(TransportAddress transportAddress) {
    nodesService.removeTransportAddress(transportAddress);
    return this;
  }

  /**
   * Closes the client.
   */
  @Override
  public void close() {
    try {
      injector.getInstance(TransportClientClusterService.class).close();
    } catch (Exception e) {
      // ignore
    }
    injector.getInstance(TransportClientNodesService.class).close();
    injector.getInstance(TransportService.class).close();

    injector.getInstance(TimerService.class).close();
    injector.getInstance(ThreadPool.class).shutdown();

    ThreadLocals.clearReferencesThreadLocals();
  }

  @Override
  public ThreadPool threadPool() {
    return internalClient.threadPool();
  }

}
