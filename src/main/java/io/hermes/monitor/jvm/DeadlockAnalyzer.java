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

package io.hermes.monitor.jvm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author spancer.ray
 */
public class DeadlockAnalyzer {

  private static final Deadlock[] NULL_RESULT = new Deadlock[0];
  private static DeadlockAnalyzer INSTANCE = new DeadlockAnalyzer();
  private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

  private DeadlockAnalyzer() {

  }

  public static DeadlockAnalyzer deadlockAnalyzer() {
    return INSTANCE;
  }

  public Deadlock[] findDeadlocks() {
    long[] deadlockedThreads = threadBean.findMonitorDeadlockedThreads();
    if (deadlockedThreads == null || deadlockedThreads.length == 0) {
      return NULL_RESULT;
    }
    ImmutableMap<Long, ThreadInfo> threadInfoMap = createThreadInfoMap(deadlockedThreads);
    Set<LinkedHashSet<ThreadInfo>> cycles = calculateCycles(threadInfoMap);
    Set<LinkedHashSet<ThreadInfo>> chains = calculateCycleDeadlockChains(threadInfoMap, cycles);
    cycles.addAll(chains);
    return createDeadlockDescriptions(cycles);
  }


  private Deadlock[] createDeadlockDescriptions(Set<LinkedHashSet<ThreadInfo>> cycles) {
    Deadlock[] result = new Deadlock[cycles.size()];
    int count = 0;
    for (LinkedHashSet<ThreadInfo> cycle : cycles) {
      ThreadInfo[] asArray = cycle.toArray(new ThreadInfo[cycle.size()]);
      Deadlock d = new Deadlock(asArray);
      result[count++] = d;
    }
    return result;
  }


  private Set<LinkedHashSet<ThreadInfo>> calculateCycles(
      ImmutableMap<Long, ThreadInfo> threadInfoMap) {
    Set<LinkedHashSet<ThreadInfo>> cycles = new HashSet<LinkedHashSet<ThreadInfo>>();
    for (Map.Entry<Long, ThreadInfo> entry : threadInfoMap.entrySet()) {
      LinkedHashSet<ThreadInfo> cycle = new LinkedHashSet<ThreadInfo>();
      for (ThreadInfo t = entry.getValue(); !cycle.contains(t); t =
          threadInfoMap.get(Long.valueOf(t.getLockOwnerId()))) {
        cycle.add(t);
      }

      cycles.add(cycle);
    }
    return cycles;
  }


  private Set<LinkedHashSet<ThreadInfo>> calculateCycleDeadlockChains(
      ImmutableMap<Long, ThreadInfo> threadInfoMap, Set<LinkedHashSet<ThreadInfo>> cycles) {
    ThreadInfo[] allThreads = threadBean.getThreadInfo(threadBean.getAllThreadIds());
    Set<LinkedHashSet<ThreadInfo>> deadlockChain = new HashSet<LinkedHashSet<ThreadInfo>>();
    Set<Long> knownDeadlockedThreads = threadInfoMap.keySet();
    for (ThreadInfo threadInfo : allThreads) {
      Thread.State state = threadInfo.getThreadState();
      if (state == Thread.State.BLOCKED
          && !knownDeadlockedThreads.contains(threadInfo.getThreadId())) {
        for (LinkedHashSet cycle : cycles) {
          if (cycle.contains(threadInfoMap.get(Long.valueOf(threadInfo.getLockOwnerId())))) {
            LinkedHashSet<ThreadInfo> chain = new LinkedHashSet<ThreadInfo>();
            for (ThreadInfo node = threadInfo; !chain.contains(node); node =
                threadInfoMap.get(Long.valueOf(node.getLockOwnerId()))) {
              chain.add(node);
            }

            deadlockChain.add(chain);
          }
        }

      }
    }

    return deadlockChain;
  }


  private ImmutableMap<Long, ThreadInfo> createThreadInfoMap(long threadIds[]) {
    ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadIds);
    ImmutableMap.Builder<Long, ThreadInfo> threadInfoMap = ImmutableMap.builder();
    for (ThreadInfo threadInfo : threadInfos) {
      threadInfoMap.put(threadInfo.getThreadId(), threadInfo);
    }
    return threadInfoMap.build();
  }


  public static class Deadlock {

    private final ThreadInfo[] members;
    private final String description;
    private final ImmutableSet<Long> memberIds;

    public Deadlock(ThreadInfo[] members) {
      this.members = members;

      ImmutableSet.Builder<Long> builder = ImmutableSet.builder();
      StringBuilder sb = new StringBuilder();
      for (int x = 0; x < members.length; x++) {
        ThreadInfo ti = members[x];
        sb.append(ti.getThreadName());
        if (x < members.length) {
          sb.append(" > ");
        }
        if (x == members.length - 1) {
          sb.append(ti.getLockOwnerName());
        }
        builder.add(ti.getThreadId());
      }
      this.description = sb.toString();
      this.memberIds = builder.build();
    }

    public ThreadInfo[] members() {
      return members;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Deadlock deadlock = (Deadlock) o;

      return memberIds != null ? memberIds.equals(deadlock.memberIds) : deadlock.memberIds == null;
    }

    @Override
    public int hashCode() {
      int result = members != null ? Arrays.hashCode(members) : 0;
      result = 31 * result + (description != null ? description.hashCode() : 0);
      result = 31 * result + (memberIds != null ? memberIds.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return description;
    }
  }
}
