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

package io.hermes.monitor.memory.alpha;

import static io.hermes.util.TimeValue.timeValueMillis;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.monitor.memory.MemoryMonitor;
import io.hermes.threadpool.ThreadPool;
import io.hermes.util.SizeUnit;
import io.hermes.util.SizeValue;
import io.hermes.util.StopWatch;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.settings.Settings;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author spancer.ray
 */
public class AlphaMemoryMonitor extends AbstractLifecycleComponent<MemoryMonitor>
    implements MemoryMonitor {

  private final double upperMemoryThreshold;

  private final double lowerMemoryThreshold;

  private final TimeValue interval;

  private final int gcThreshold;

  private final int cleanThreshold;

  private final SizeValue minimumFlushableSizeToClean;

  private final int translogNumberOfOperationsThreshold;

  private final ThreadPool threadPool;

  private final Runtime runtime;

  private final SizeValue maxMemory;

  private final SizeValue totalMemory;

  private volatile ScheduledFuture scheduledFuture;

  private AtomicLong totalCleans = new AtomicLong();
  private AtomicLong totalGCs = new AtomicLong();

  @Inject
  public AlphaMemoryMonitor(Settings settings, ThreadPool threadPool) {
    super(settings);
    this.threadPool = threadPool;
    this.upperMemoryThreshold = componentSettings.getAsDouble("upper_memory_threshold", 0.8);
    this.lowerMemoryThreshold = componentSettings.getAsDouble("lower_memory_threshold", 0.5);
    this.interval = componentSettings.getAsTime("interval", timeValueMillis(500));
    this.gcThreshold = componentSettings.getAsInt("gc_threshold", 5);
    this.cleanThreshold = componentSettings.getAsInt("clean_threshold", 10);
    this.minimumFlushableSizeToClean = componentSettings
        .getAsSize("minimum_flushable_size_to_clean", new SizeValue(5, SizeUnit.MB));
    this.translogNumberOfOperationsThreshold =
        componentSettings.getAsInt("translog_number_of_operations_threshold", 5000);

    logger.debug("interval[" + interval + "], upper_memory_threshold[" + upperMemoryThreshold
        + "], lower_memory_threshold[" + lowerMemoryThreshold
        + "], translog_number_of_operations_threshold[" + translogNumberOfOperationsThreshold
        + "]");

    this.runtime = Runtime.getRuntime();
    this.maxMemory = new SizeValue(runtime.maxMemory());
    this.totalMemory =
        maxMemory.bytes() == runtime.totalMemory() ? new SizeValue(runtime.totalMemory())
            : null; // Xmx==Xms
    // when
    // the
    // JVM
    // was
    // started.
  }

  @Override
  protected void doStart() throws HermesException {
    scheduledFuture = threadPool.scheduleWithFixedDelay(new MemoryCleaner(), interval);
  }

  @Override
  protected void doStop() throws HermesException {
    scheduledFuture.cancel(true);
  }

  @Override
  protected void doClose() throws HermesException {
  }

  private long freeMemory() {
    return runtime.freeMemory();
  }

  private long totalMemory() {
    return totalMemory == null ? runtime.totalMemory() : totalMemory.bytes();
  }

  private class MemoryCleaner implements Runnable {

    private int gcCounter;

    private boolean performedClean;

    private int cleanCounter;

    private StopWatch stopWatch = new StopWatch().keepTaskList(false);

    @Override
    public void run() {
      long upperMemory = maxMemory.bytes();
      long totalMemory = totalMemory();
      long usedMemory = totalMemory - freeMemory();

      if (performedClean) {
        if (++cleanCounter < cleanThreshold) {
          return;
        }
      }

      long totalClean = totalCleans.incrementAndGet();

      long lowerThresholdMemory = (long) (upperMemory * lowerMemoryThreshold);
      long memoryToClean = usedMemory - lowerThresholdMemory;
      if (logger.isDebugEnabled()) {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(totalClean).append("]: ");
        sb.append("Cleaning, memoryToClean[").append(new SizeValue(memoryToClean)).append(']');
        sb.append(", lowerMemoryThreshold[").append(new SizeValue(lowerThresholdMemory))
            .append(']');
        sb.append(", usedMemory[").append(new SizeValue(usedMemory)).append(']');
        sb.append(", totalMemory[").append(new SizeValue(totalMemory)).append(']');
        sb.append(", maxMemory[").append(maxMemory).append(']');
        logger.debug(sb.toString());
      }

      performedClean = true;
      cleanCounter = 0;

      if (++gcCounter >= gcThreshold) {
        long totalGc = totalGCs.incrementAndGet();
        logger.debug("[" + totalGc + "]: Running GC after [" + gcCounter + "] memory clean swipes");
        System.gc();
        gcCounter = 0;
      }

    }
  }
}
