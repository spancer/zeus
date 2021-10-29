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

package io.hermes.monitor.dump.thread;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.hermes.monitor.dump.Dump;
import io.hermes.monitor.dump.DumpContributionFailedException;
import io.hermes.monitor.dump.DumpContributor;
import io.hermes.util.settings.Settings;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * @author spancer.ray
 */
public class ThreadDumpContributor implements DumpContributor {

  public static final String THREAD_DUMP = "thread";
  private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
  private final String name;

  @Inject
  public ThreadDumpContributor(@Assisted String name, @Assisted Settings settings) {
    this.name = name;
  }

  private static MonitorInfo findLockedMonitor(StackTraceElement stackTraceElement,
      MonitorInfo lockedMonitors[]) {
    for (MonitorInfo monitorInfo : lockedMonitors) {
      if (stackTraceElement.equals(monitorInfo.getLockedStackFrame())) {
        return monitorInfo;
      }
    }

    return null;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void contribute(Dump dump) throws DumpContributionFailedException {
    PrintWriter writer = new PrintWriter(dump.createFileWriter("threads.txt"));
    try {
      processDeadlocks(writer);
      processAllThreads(writer);
    } catch (Exception e) {
      throw new DumpContributionFailedException(getName(), "Failed to generate", e);
    } finally {
      try {
        writer.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  private void processDeadlocks(PrintWriter dump) {
    dump.println("=====  Deadlocked Threads =====");
    long[] deadlockedThreadIds = findDeadlockedThreads();
    if (deadlockedThreadIds != null) {
      dumpThreads(dump, getThreadInfo(deadlockedThreadIds));
    }
  }

  private void processAllThreads(PrintWriter dump) {
    dump.println();
    dump.println("===== All Threads =====");
    dumpThreads(dump, dumpAllThreads());
  }

  private void dumpThreads(PrintWriter dump, ThreadInfo infos[]) {
    for (ThreadInfo info : infos) {
      dump.println();
      write(info, dump);
    }
  }

  private ThreadInfo[] dumpAllThreads() {
    return threadBean.dumpAllThreads(true, true);
  }

  public long[] findDeadlockedThreads() {
    return threadBean.findDeadlockedThreads();
  }

  public ThreadInfo[] getThreadInfo(long[] threadIds) {
    return threadBean.getThreadInfo(threadIds, true, true);
  }

  private void write(ThreadInfo threadInfo, PrintWriter writer) {
    writer.print(String.format("\"%s\" Id=%s %s", threadInfo.getThreadName(),
        threadInfo.getThreadId(), threadInfo.getThreadState()));
    if (threadInfo.getLockName() != null) {
      writer.print(String.format(" on %s", threadInfo.getLockName()));
      if (threadInfo.getLockOwnerName() != null) {
        writer.print(String.format(" owned by \"%s\" Id=%s", threadInfo.getLockOwnerName(),
            threadInfo.getLockOwnerId()));
      }
    }
    if (threadInfo.isInNative()) {
      writer.println(" (in native)");
    } else {
      writer.println();
    }
    MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
    StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
    for (StackTraceElement stackTraceElement : stackTraceElements) {
      writer.println("    at " + stackTraceElement);
      MonitorInfo lockedMonitor = findLockedMonitor(stackTraceElement, lockedMonitors);
      if (lockedMonitor != null) {
        writer.println(("    - locked " + lockedMonitor.getClassName() + "@"
            + lockedMonitor.getIdentityHashCode()));
      }
    }

  }
}
