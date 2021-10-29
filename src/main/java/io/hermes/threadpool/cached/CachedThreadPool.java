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

package io.hermes.threadpool.cached;

import static io.hermes.util.TimeValue.timeValueSeconds;
import static io.hermes.util.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;

import com.google.inject.Inject;
import io.hermes.threadpool.support.AbstractThreadPool;
import io.hermes.util.TimeValue;
import io.hermes.util.concurrent.DynamicExecutors;
import io.hermes.util.settings.Settings;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A thread pool that will create an unbounded number of threads.
 *
 * @author spancer.ray
 */
public class CachedThreadPool extends AbstractThreadPool {

  private final TimeValue keepAlive;

  private final int scheduledSize;

  public CachedThreadPool() {
    this(EMPTY_SETTINGS);
  }

  @Inject
  public CachedThreadPool(Settings settings) {
    super(settings);
    this.scheduledSize = componentSettings.getAsInt("scheduled_size", 20);
    this.keepAlive = componentSettings.getAsTime("keep_alive", timeValueSeconds(60));
    logger.debug("Initializing {} thread pool with keep_alive[{}], scheduled_size[{}]",
        getType(), keepAlive, scheduledSize);
    executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, keepAlive.millis(),
        TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(),
        DynamicExecutors.daemonThreadFactory(settings, "[tp]"));
    scheduledExecutorService = Executors.newScheduledThreadPool(scheduledSize,
        DynamicExecutors.daemonThreadFactory(settings, "[sc]"));
    started = true;
  }

  @Override
  public String getType() {
    return "cached";
  }
}
