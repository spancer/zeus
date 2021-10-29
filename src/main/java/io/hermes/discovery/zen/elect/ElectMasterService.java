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

package io.hermes.discovery.zen.elect;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

import com.google.common.collect.Lists;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.Settings;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author spancer.ray
 */
public class ElectMasterService extends AbstractComponent {

  private final NodeComparator nodeComparator = new NodeComparator();

  public ElectMasterService(Settings settings) {
    super(settings);
  }

  /**
   * Returns a list of the next possible masters.
   */
  public DiscoveryNode[] nextPossibleMasters(Iterable<DiscoveryNode> nodes,
      int numberOfPossibleMasters) {
    List<DiscoveryNode> sortedNodes = sortedMasterNodes(nodes);
    if (sortedNodes == null) {
      return new DiscoveryNode[0];
    }
    List<DiscoveryNode> nextPossibleMasters = newArrayListWithExpectedSize(numberOfPossibleMasters);
    int counter = 0;
    for (DiscoveryNode nextPossibleMaster : sortedNodes) {
      if (++counter >= numberOfPossibleMasters) {
        break;
      }
      nextPossibleMasters.add(nextPossibleMaster);
    }
    return nextPossibleMasters.toArray(new DiscoveryNode[nextPossibleMasters.size()]);
  }

  /**
   * Elects a new master out of the possible nodes, returning it. Returns <tt>null</tt> if no master
   * has been elected.
   */
  public DiscoveryNode electMaster(Iterable<DiscoveryNode> nodes) {
    List<DiscoveryNode> sortedNodes = sortedMasterNodes(nodes);
    if (sortedNodes == null || sortedNodes.isEmpty()) {
      return null;
    }
    return sortedNodes.get(0);
  }

  private List<DiscoveryNode> sortedMasterNodes(Iterable<DiscoveryNode> nodes) {
    List<DiscoveryNode> possibleNodes = Lists.newArrayList(nodes);
    if (possibleNodes.isEmpty()) {
      return null;
    }
    // clean non master nodes
    for (Iterator<DiscoveryNode> it = possibleNodes.iterator(); it.hasNext(); ) {
      DiscoveryNode node = it.next();
      if (node.attributes().containsKey("zen.master")) {
        if (node.attributes().get("zen.master").equals("false")) {
          it.remove();
        }
      }
    }
    Collections.sort(possibleNodes, nodeComparator);
    return possibleNodes;
  }

  private static class NodeComparator implements Comparator<DiscoveryNode> {

    @Override
    public int compare(DiscoveryNode o1, DiscoveryNode o2) {
      return o1.id().compareTo(o2.id());
    }
  }
}
