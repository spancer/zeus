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

package io.hermes.timer;

import static io.hermes.util.TimeValue.timeValueMillis;
import static io.hermes.util.concurrent.DynamicExecutors.daemonThreadFactory;

import com.google.inject.Inject;
import io.hermes.threadpool.ThreadPool;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.ImmutableSettings;
import io.hermes.util.settings.Settings;
import io.hermes.util.timer.HashedWheelTimer;
import io.hermes.util.timer.Timeout;
import io.hermes.util.timer.Timer;
import io.hermes.util.timer.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author spancer.ray
 */
public class TimerService extends AbstractComponent {

  private final ThreadPool threadPool;

  private final TimeEstimator timeEstimator;

  private final ScheduledFuture timeEstimatorFuture;

  private final Timer timer;

  private final TimeValue tickDuration;

  private final int ticksPerWheel;

  public TimerService(ThreadPool threadPool) {
    this(ImmutableSettings.Builder.EMPTY_SETTINGS, threadPool);
  }

  @Inject
  public TimerService(Settings settings, ThreadPool threadPool) {
    super(settings);
    this.threadPool = threadPool;

    this.timeEstimator = new TimeEstimator();
    this.timeEstimatorFuture =
        threadPool.scheduleWithFixedDelay(timeEstimator, 50, 50, TimeUnit.MILLISECONDS);

    this.tickDuration = componentSettings.getAsTime("tick_duration", timeValueMillis(100));
    this.ticksPerWheel = componentSettings.getAsInt("ticks_per_wheel", 1024);

    this.timer = new HashedWheelTimer(logger, daemonThreadFactory(settings, "timer"),
        tickDuration.millis(), TimeUnit.MILLISECONDS, ticksPerWheel);
  }

  public void close() {
    timeEstimatorFuture.cancel(true);
    timer.stop();
  }

  public long estimatedTimeInMillis() {
    return timeEstimator.time();
  }

  public Timeout newTimeout(TimerTask task, TimeValue delay) {
    return newTimeout(task, delay.nanos(), TimeUnit.NANOSECONDS);
  }

  public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
    return timer.newTimeout(task, delay, unit);
  }

  private static class TimeEstimator implements Runnable {

    private long time = System.currentTimeMillis();

    @Override
    public void run() {
      this.time = System.currentTimeMillis();
    }

    public long time() {
      return this.time;
    }
  }
}
