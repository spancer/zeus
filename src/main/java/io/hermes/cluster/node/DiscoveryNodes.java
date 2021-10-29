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

package io.hermes.cluster.node;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import io.hermes.util.Nullable;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.transport.TransportAddress;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spancer.ray
 */
public class DiscoveryNodes implements Iterable<DiscoveryNode> {

  public static DiscoveryNodes EMPTY_NODES = newNodesBuilder().build();

  private final ImmutableMap<String, DiscoveryNode> nodes;

  private final ImmutableMap<String, DiscoveryNode> dataNodes;

  private final String masterNodeId;

  private final String localNodeId;

  private DiscoveryNodes(ImmutableMap<String, DiscoveryNode> nodes,
      ImmutableMap<String, DiscoveryNode> dataNodes, String masterNodeId, String localNodeId) {
    this.nodes = nodes;
    this.dataNodes = dataNodes;
    this.masterNodeId = masterNodeId;
    this.localNodeId = localNodeId;
  }

  public static Builder newNodesBuilder() {
    return new Builder();
  }

  @Override
  public UnmodifiableIterator<DiscoveryNode> iterator() {
    return nodes.values().iterator();
  }

  /**
   * Is this a valid nodes that has the minimal information set. The minimal set is defined by the
   * localNodeId being set.
   */
  public boolean valid() {
    return localNodeId != null;
  }

  /**
   * Returns <tt>true</tt> if the local node is the master node.
   */
  public boolean localNodeMaster() {
    if (localNodeId == null) {
      // we don't know yet the local node id, return false
      return false;
    }
    return localNodeId.equals(masterNodeId);
  }

  public int size() {
    return nodes.size();
  }

  public int getSize() {
    return size();
  }

  public ImmutableMap<String, DiscoveryNode> nodes() {
    return this.nodes;
  }

  public ImmutableMap<String, DiscoveryNode> getNodes() {
    return nodes();
  }

  public ImmutableMap<String, DiscoveryNode> dataNodes() {
    return this.dataNodes;
  }

  public ImmutableMap<String, DiscoveryNode> getDataNodes() {
    return dataNodes();
  }

  public DiscoveryNode get(String nodeId) {
    return nodes.get(nodeId);
  }

  public boolean nodeExists(String nodeId) {
    return nodes.containsKey(nodeId);
  }

  public String masterNodeId() {
    return this.masterNodeId;
  }

  public String getMasterNodeId() {
    return masterNodeId();
  }

  public String localNodeId() {
    return this.localNodeId;
  }

  public String getLocalNodeId() {
    return localNodeId();
  }

  public DiscoveryNode localNode() {
    return nodes.get(localNodeId);
  }

  public DiscoveryNode getLocalNode() {
    return localNode();
  }

  public DiscoveryNode masterNode() {
    return nodes.get(masterNodeId);
  }

  public DiscoveryNode getMasterNode() {
    return masterNode();
  }

  public DiscoveryNode findByAddress(TransportAddress address) {
    for (DiscoveryNode node : nodes.values()) {
      if (node.address().equals(address)) {
        return node;
      }
    }
    return null;
  }

  public DiscoveryNodes removeDeadMembers(Set<String> newNodes, String masterNodeId) {
    Builder builder = new Builder().masterNodeId(masterNodeId).localNodeId(localNodeId);
    for (DiscoveryNode node : this) {
      if (newNodes.contains(node.id())) {
        builder.put(node);
      }
    }
    return builder.build();
  }

  public DiscoveryNodes newNode(DiscoveryNode node) {
    return new Builder().putAll(this).put(node).build();
  }

  /**
   * Returns the changes comparing this nodes to the provided nodes.
   */
  public Delta delta(DiscoveryNodes other) {
    List<DiscoveryNode> removed = newArrayList();
    List<DiscoveryNode> added = newArrayList();
    for (DiscoveryNode node : other) {
      if (!this.nodeExists(node.id())) {
        removed.add(node);
      }
    }
    for (DiscoveryNode node : this) {
      if (!other.nodeExists(node.id())) {
        added.add(node);
      }
    }
    DiscoveryNode previousMasterNode = null;
    DiscoveryNode newMasterNode = null;
    if (masterNodeId != null) {
      if (other.masterNodeId == null || !other.masterNodeId.equals(masterNodeId)) {
        previousMasterNode = other.masterNode();
        newMasterNode = masterNode();
      }
    }
    return new Delta(previousMasterNode, newMasterNode, localNodeId, ImmutableList.copyOf(removed),
        ImmutableList.copyOf(added));
  }

  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    sb.append("Nodes: \n");
    for (DiscoveryNode node : this) {
      sb.append("   ").append(node);
      if (node == localNode()) {
        sb.append(", local");
      }
      if (node == masterNode()) {
        sb.append(", master");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public Delta emptyDelta() {
    return new Delta(null, null, localNodeId, DiscoveryNode.EMPTY_LIST, DiscoveryNode.EMPTY_LIST);
  }

  public static class Delta {

    private final String localNodeId;
    private final DiscoveryNode previousMasterNode;
    private final DiscoveryNode newMasterNode;
    private final ImmutableList<DiscoveryNode> removed;
    private final ImmutableList<DiscoveryNode> added;


    public Delta(String localNodeId, ImmutableList<DiscoveryNode> removed,
        ImmutableList<DiscoveryNode> added) {
      this(null, null, localNodeId, removed, added);
    }

    public Delta(@Nullable DiscoveryNode previousMasterNode, @Nullable DiscoveryNode newMasterNode,
        String localNodeId, ImmutableList<DiscoveryNode> removed,
        ImmutableList<DiscoveryNode> added) {
      this.previousMasterNode = previousMasterNode;
      this.newMasterNode = newMasterNode;
      this.localNodeId = localNodeId;
      this.removed = removed;
      this.added = added;
    }

    public boolean hasChanges() {
      return masterNodeChanged() || !removed.isEmpty() || !added.isEmpty();
    }

    public boolean masterNodeChanged() {
      return newMasterNode != null;
    }

    public DiscoveryNode previousMasterNode() {
      return previousMasterNode;
    }

    public DiscoveryNode newMasterNode() {
      return newMasterNode;
    }

    public boolean removed() {
      return !removed.isEmpty();
    }

    public ImmutableList<DiscoveryNode> removedNodes() {
      return removed;
    }

    public boolean added() {
      return !added.isEmpty();
    }

    public ImmutableList<DiscoveryNode> addedNodes() {
      return added;
    }

    public String shortSummary() {
      StringBuilder sb = new StringBuilder();
      if (!removed() && masterNodeChanged()) {
        if (newMasterNode.id().equals(localNodeId)) {
          // we are the master, no nodes we removed, we are actually the first master
          sb.append("New Master ").append(newMasterNode());
        } else {
          // we are not the master, so we just got this event. No nodes were removed, so its not a
          // *new* master
          sb.append("Detected Master ").append(newMasterNode());
        }
      } else {
        if (masterNodeChanged()) {
          sb.append("Master {New ").append(newMasterNode());
          if (previousMasterNode() != null) {
            sb.append(", Previous ").append(previousMasterNode());
          }
          sb.append("}");
        }
        if (removed()) {
          if (masterNodeChanged()) {
            sb.append(", ");
          }
          sb.append("Removed {");
          for (DiscoveryNode node : removedNodes()) {
            sb.append(node).append(',');
          }
          sb.append("}");
        }
      }
      if (added()) {
        // don't print if there is one added, and it is us
        if (!(addedNodes().size() == 1 && addedNodes().get(0).id().equals(localNodeId))) {
          if (removed() || masterNodeChanged()) {
            sb.append(", ");
          }
          sb.append("Added {");
          for (DiscoveryNode node : addedNodes()) {
            if (!node.id().equals(localNodeId)) {
              // don't print ourself
              sb.append(node).append(',');
            }
          }
          sb.append("}");
        }
      }
      return sb.toString();
    }
  }

  public static class Builder {

    private Map<String, DiscoveryNode> nodes = newHashMap();

    private String masterNodeId;

    private String localNodeId;

    public static void writeTo(DiscoveryNodes nodes, StreamOutput out) throws IOException {
      out.writeUTF(nodes.masterNodeId);
      out.writeVInt(nodes.size());
      for (DiscoveryNode node : nodes) {
        node.writeTo(out);
      }
    }

    public static DiscoveryNodes readFrom(StreamInput in, @Nullable DiscoveryNode localNode)
        throws IOException {
      Builder builder = new Builder();
      builder.masterNodeId(in.readUTF());
      if (localNode != null) {
        builder.localNodeId(localNode.id());
      }
      int size = in.readVInt();
      for (int i = 0; i < size; i++) {
        DiscoveryNode node = DiscoveryNode.readNode(in);
        if (localNode != null && node.id().equals(localNode.id())) {
          // reuse the same instance of our address and local node id for faster equality
          node = localNode;
        }
        builder.put(node);
      }
      return builder.build();
    }

    public Builder putAll(DiscoveryNodes nodes) {
      this.masterNodeId = nodes.masterNodeId();
      this.localNodeId = nodes.localNodeId();
      for (DiscoveryNode node : nodes) {
        put(node);
      }
      return this;
    }

    public Builder put(DiscoveryNode node) {
      nodes.put(node.id(), node);
      return this;
    }

    public Builder putAll(Iterable<DiscoveryNode> nodes) {
      for (DiscoveryNode node : nodes) {
        put(node);
      }
      return this;
    }

    public Builder remove(String nodeId) {
      nodes.remove(nodeId);
      return this;
    }

    public Builder masterNodeId(String masterNodeId) {
      this.masterNodeId = masterNodeId;
      return this;
    }

    public Builder localNodeId(String localNodeId) {
      this.localNodeId = localNodeId;
      return this;
    }

    public DiscoveryNodes build() {
      ImmutableMap.Builder<String, DiscoveryNode> dataNodesBuilder = ImmutableMap.builder();
      for (Map.Entry<String, DiscoveryNode> nodeEntry : nodes.entrySet()) {
        if (nodeEntry.getValue().dataNode()) {
          dataNodesBuilder.put(nodeEntry.getKey(), nodeEntry.getValue());
        }
      }
      return new DiscoveryNodes(ImmutableMap.copyOf(nodes), dataNodesBuilder.build(), masterNodeId,
          localNodeId);
    }
  }
}
