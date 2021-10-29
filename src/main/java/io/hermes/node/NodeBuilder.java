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

package io.hermes.node;

import io.hermes.node.internal.InternalNode;
import io.hermes.util.settings.ImmutableSettings;
import io.hermes.util.settings.Settings;

/**
 * A node builder is used to construct a {@link Node} instance.
 *
 * <p>
 * Settings will be loaded relative to the Hermes home (with or without <tt>config/</tt> prefix) and
 * if not found, within the classpath (with or without
 * <tt>config/<tt> prefix). The settings file loaded can either be named
 * <tt>hermes.yml</tt> or <tt>hermes.json</tt>). Loading settings can be disabled by calling
 * {@link #loadConfigSettings(boolean)} with <tt>false<tt>.
 *
 * <p>Explicit settings can be passed by using the {@link #settings(Settings)} method.
 *
 * <p>In any case, settings will be resolved from system properties as well that are either
 * prefixed with <tt>hermes.</tt>.
 *
 * <p>
 * An example for creating a simple node with optional settings loaded from the classpath:
 *
 * <pre>
 * Node node = NodeBuilder.nodeBuilder().node();
 * </pre>
 *
 * <p>
 * An example for creating a node with explicit settings (in this case, a node in the cluster that
 * does not hold data):
 *
 * <pre>
 * Node node = NodeBuilder.nodeBuilder()
 *                      .settings(ImmutableSettings.settingsBuilder().put("node.data", false)
 *                      .node();
 * </pre>
 *
 * <p>
 * When done with the node, make sure you call {@link Node#close()} on it.
 *
 * @author spancer.ray
 */
public class NodeBuilder {

  private final ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();

  private boolean loadConfigSettings = true;

  /**
   * A convenient factory method to create a {@link NodeBuilder}.
   */
  public static NodeBuilder nodeBuilder() {
    return new NodeBuilder();
  }

  /**
   * Explicit node settings to set.
   */
  public NodeBuilder settings(Settings.Builder settings) {
    return settings(settings.build());
  }

  /**
   * Explicit node settings to set.
   */
  public NodeBuilder settings(Settings settings) {
    this.settings.put(settings);
    return this;
  }

  /**
   * Should the node builder automatically try and load config settings from the file system /
   * classpath. Defaults to <tt>true</tt>.
   */
  public NodeBuilder loadConfigSettings(boolean loadConfigSettings) {
    this.loadConfigSettings = loadConfigSettings;
    return this;
  }

  /**
   * Is the node going to be a client node which means it will hold no data (<tt>node.data</tt> is
   * set to <tt>false</tt>) and other optimizations by different modules.
   *
   * @param client Should the node be just a client node or not.
   */
  public NodeBuilder client(boolean client) {
    settings.put("node.client", client);
    return this;
  }

  /**
   * Is the node going to be allowed to allocate data (shards) to it or not. This setting map to
   * the
   * <tt>node.data</tt> setting. Note, when setting {@link #client(boolean)}, the node will not
   * hold any data by default.
   *
   * @param data Should the node be allocated data to or not.
   */
  public NodeBuilder data(boolean data) {
    settings.put("node.data", data);
    return this;
  }

  /**
   * Is the node a local node. A local node is a node that uses a local (JVM level) discovery and
   * transport. Other (local) nodes started within the same JVM (actually, class-loader) will be
   * discovered and communicated with. Nodes outside of the JVM will not be discovered.
   *
   * @param local Should the node be local or not
   */
  public NodeBuilder local(boolean local) {
    settings.put("node.local", local);
    return this;
  }

  /**
   * The cluster name this node is part of (maps to the <tt>cluster.name</tt> setting). Defaults to
   * <tt>hermes</tt>.
   *
   * @param clusterName The cluster name this node is part of.
   */
  public NodeBuilder clusterName(String clusterName) {
    settings.put("cluster.name", clusterName);
    return this;
  }

  /**
   * Builds the node without starting it.
   */
  public Node build() {
    return new InternalNode(settings.build(), loadConfigSettings);
  }

  /**
   * {@link #build()}s and starts the node.
   */
  public Node node() {
    return build().start();
  }
}
