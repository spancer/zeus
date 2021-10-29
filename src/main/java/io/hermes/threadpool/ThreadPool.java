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

package io.hermes.threadpool;

import io.hermes.util.TimeValue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author spancer.ray
 */
public interface ThreadPool extends ScheduledExecutorService {

  boolean isStarted();

  <T> Future<T> submit(Callable<T> task, FutureListener<T> listener);

  <T> Future<T> submit(Runnable task, T result, FutureListener<T> listener);

  Future<?> submit(Runnable task, FutureListener<?> listener);

  ScheduledFuture<?> schedule(Runnable command, TimeValue delay);

  ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, TimeValue interval);
}
