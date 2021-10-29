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

import static io.hermes.monitor.dump.summary.SummaryDumpContributor.SUMMARY;
import static io.hermes.monitor.dump.thread.ThreadDumpContributor.THREAD_DUMP;
import static io.hermes.monitor.jvm.DeadlockAnalyzer.deadlockAnalyzer;
import static io.hermes.monitor.jvm.JvmStats.jvmStats;
import static io.hermes.util.TimeValue.timeValueSeconds;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.monitor.dump.DumpGenerator;
import io.hermes.monitor.dump.DumpMonitorService;
import io.hermes.threadpool.ThreadPool;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.settings.Settings;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * @author spancer.ray
 */
public class JvmMonitorService extends AbstractLifecycleComponent<JvmMonitorService> {

  private final ThreadPool threadPool;

  private final DumpMonitorService dumpMonitorService;

  private final boolean enabled;

  private final TimeValue interval;

  private final TimeValue gcCollectionWarning;

  private volatile ScheduledFuture scheduledFuture;

  @Inject
  public JvmMonitorService(Settings settings, ThreadPool threadPool,
      DumpMonitorService dumpMonitorService) {
    super(settings);
    this.threadPool = threadPool;
    this.dumpMonitorService = dumpMonitorService;

    this.enabled = componentSettings.getAsBoolean("enabled", true);
    this.interval = componentSettings.getAsTime("interval", timeValueSeconds(10));
    this.gcCollectionWarning =
        componentSettings.getAsTime("gcCollectionWarning", timeValueSeconds(10));
  }

  @Override
  protected void doStart() throws HermesException {
    if (!enabled) {
      return;
    }
    scheduledFuture = threadPool.scheduleWithFixedDelay(new JvmMonitor(), interval);
  }

  @Override
  protected void doStop() throws HermesException {
    if (!enabled) {
      return;
    }
    scheduledFuture.cancel(true);
  }

  @Override
  protected void doClose() throws HermesException {
  }

  private class JvmMonitor implements Runnable {

    private final Set<DeadlockAnalyzer.Deadlock> lastSeenDeadlocks =
        new HashSet<DeadlockAnalyzer.Deadlock>();
    private JvmStats lastJvmStats = jvmStats();

    public JvmMonitor() {
    }

    @Override
    public void run() {
      monitorDeadlock();
      monitorLongGc();
    }

    private void monitorLongGc() {
      JvmStats currentJvmStats = jvmStats();
      long collectionTime =
          currentJvmStats.gcCollectionTime().millis() - lastJvmStats.gcCollectionTime().millis();
      if (collectionTime > gcCollectionWarning.millis()) {
        logger.warn("Long GC collection occurred, took [" + new TimeValue(collectionTime)
            + "], breached threshold [" + gcCollectionWarning + "]");
      }
      lastJvmStats = currentJvmStats;
    }

    private void monitorDeadlock() {
      DeadlockAnalyzer.Deadlock[] deadlocks = deadlockAnalyzer().findDeadlocks();
      if (deadlocks != null && deadlocks.length > 0) {
        ImmutableSet<DeadlockAnalyzer.Deadlock> asSet =
            new ImmutableSet.Builder<DeadlockAnalyzer.Deadlock>().add(deadlocks).build();
        if (!asSet.equals(lastSeenDeadlocks)) {
          DumpGenerator.Result genResult =
              dumpMonitorService.generateDump("deadlock", null, SUMMARY, THREAD_DUMP);
          StringBuilder sb = new StringBuilder("Detected Deadlock(s)");
          for (DeadlockAnalyzer.Deadlock deadlock : asSet) {
            sb.append("\n   ----> ").append(deadlock);
          }
          sb.append("\nDump generated [").append(genResult.location()).append("]");
          logger.error(sb.toString());
          lastSeenDeadlocks.clear();
          lastSeenDeadlocks.addAll(asSet);
        }
      } else {
        lastSeenDeadlocks.clear();
      }
    }
  }
}
