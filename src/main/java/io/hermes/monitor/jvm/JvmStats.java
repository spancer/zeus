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

import io.hermes.util.SizeValue;
import io.hermes.util.TimeValue;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author spancer.ray
 */
public class JvmStats implements Streamable, Serializable {

  private static RuntimeMXBean runtimeMXBean;
  private static MemoryMXBean memoryMXBean;
  private static ThreadMXBean threadMXBean;

  static {
    runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    memoryMXBean = ManagementFactory.getMemoryMXBean();
    threadMXBean = ManagementFactory.getThreadMXBean();
  }

  private long timestamp = -1;
  private long uptime;
  private long memoryHeapCommitted;
  private long memoryHeapUsed;
  private long memoryNonHeapCommitted;
  private long memoryNonHeapUsed;
  private int threadCount;
  private int peakThreadCount;
  private long gcCollectionCount;
  private long gcCollectionTime;

  private JvmStats() {
  }

  public JvmStats(long timestamp, long uptime, long memoryHeapCommitted, long memoryHeapUsed,
      long memoryNonHeapCommitted, long memoryNonHeapUsed, int threadCount, int peakThreadCount,
      long gcCollectionCount, long gcCollectionTime) {
    this.timestamp = timestamp;
    this.uptime = uptime;
    this.memoryHeapCommitted = memoryHeapCommitted;
    this.memoryHeapUsed = memoryHeapUsed;
    this.memoryNonHeapCommitted = memoryNonHeapCommitted;
    this.memoryNonHeapUsed = memoryNonHeapUsed;
    this.threadCount = threadCount;
    this.peakThreadCount = peakThreadCount;
    this.gcCollectionCount = gcCollectionCount;
    this.gcCollectionTime = gcCollectionTime;
  }

  public static JvmStats jvmStats() {
    long gcCollectionCount = 0;
    long gcCollectionTime = 0;
    List<GarbageCollectorMXBean> gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
      long tmp = gcMxBean.getCollectionCount();
      if (tmp != -1) {
        gcCollectionCount += tmp;
      }
      tmp = gcMxBean.getCollectionTime();
      if (tmp != -1) {
        gcCollectionTime += tmp;
      }
    }
    return new JvmStats(System.currentTimeMillis(), runtimeMXBean.getUptime(),
        memoryMXBean.getHeapMemoryUsage().getCommitted(),
        memoryMXBean.getHeapMemoryUsage().getUsed(),
        memoryMXBean.getNonHeapMemoryUsage().getCommitted(),
        memoryMXBean.getNonHeapMemoryUsage().getUsed(), threadMXBean.getThreadCount(),
        threadMXBean.getPeakThreadCount(), gcCollectionCount, gcCollectionTime);
  }

  public static JvmStats readJvmStats(StreamInput in) throws IOException {
    JvmStats jvmStats = new JvmStats();
    jvmStats.readFrom(in);
    return jvmStats;
  }

  public long timestamp() {
    return timestamp;
  }

  public long uptime() {
    return uptime;
  }

  public SizeValue memoryHeapCommitted() {
    return new SizeValue(memoryHeapCommitted);
  }

  public SizeValue memoryHeapUsed() {
    return new SizeValue(memoryHeapUsed);
  }

  public SizeValue memoryNonHeapCommitted() {
    return new SizeValue(memoryNonHeapCommitted);
  }

  public SizeValue memoryNonHeapUsed() {
    return new SizeValue(memoryNonHeapUsed);
  }

  public int threadCount() {
    return threadCount;
  }

  public int peakThreadCount() {
    return peakThreadCount;
  }

  public long gcCollectionCount() {
    return gcCollectionCount;
  }

  public TimeValue gcCollectionTime() {
    return new TimeValue(gcCollectionTime, TimeUnit.MILLISECONDS);
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    timestamp = in.readVLong();
    uptime = in.readVLong();
    memoryHeapCommitted = in.readVLong();
    memoryHeapUsed = in.readVLong();
    memoryNonHeapCommitted = in.readVLong();
    memoryNonHeapUsed = in.readVLong();
    threadCount = in.readVInt();
    peakThreadCount = in.readVInt();
    gcCollectionCount = in.readVLong();
    gcCollectionTime = in.readVLong();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeVLong(timestamp);
    out.writeVLong(uptime);
    out.writeVLong(memoryHeapCommitted);
    out.writeVLong(memoryHeapUsed);
    out.writeVLong(memoryNonHeapCommitted);
    out.writeVLong(memoryNonHeapUsed);
    out.writeVInt(threadCount);
    out.writeVInt(peakThreadCount);
    out.writeVLong(gcCollectionCount);
    out.writeVLong(gcCollectionTime);
  }
}
