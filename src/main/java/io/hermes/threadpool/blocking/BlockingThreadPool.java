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
/*
 * Copyright (c) Chalco-Steering Technology Co., Ltd. All Rights Reserved. This software is licensed
 * not sold. Use or reproduction of this software by any unauthorized individual or entity is
 * strictly prohibited. This software is the confidential and proprietary information of
 * Chalco-Steering Technology Co., Ltd. Disclosure of such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into with Chalco-Steering
 * Technology Co., Ltd. Chalco-Steering Technology Co., Ltd. MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * Chalco-Steering Technology Co., Ltd. SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ANY DERIVATIVES THEREOF.
 */
package io.hermes.threadpool.blocking;

import static io.hermes.util.TimeValue.timeValueSeconds;
import static io.hermes.util.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import java.util.concurrent.Executors;
import com.google.inject.Inject;
import io.hermes.threadpool.support.AbstractThreadPool;
import io.hermes.util.SizeUnit;
import io.hermes.util.SizeValue;
import io.hermes.util.TimeValue;
import io.hermes.util.concurrent.DynamicExecutors;
import io.hermes.util.settings.Settings;

/**
 * A thread pool that will block the execute if all threads are busy.
 *
 * @author spancer.ray
 */
public class BlockingThreadPool extends AbstractThreadPool {

  private final int min;
  private final int max;
  private final int capacity;
  private final TimeValue waitTime;
  private final TimeValue keepAlive;

  private final int scheduledSize;

  public BlockingThreadPool() {
    this(EMPTY_SETTINGS);
  }

  @Inject
  public BlockingThreadPool(Settings settings) {
    super(settings);
    this.scheduledSize = componentSettings.getAsInt("scheduled_size", 20);
    this.min = componentSettings.getAsInt("min", 1);
    this.max = componentSettings.getAsInt("max", 100);
    this.capacity =
        (int) componentSettings.getAsSize("capacity", new SizeValue(1, SizeUnit.KB)).bytes();
    this.waitTime = componentSettings.getAsTime("wait_time", timeValueSeconds(60));
    this.keepAlive = componentSettings.getAsTime("keep_alive", timeValueSeconds(60));
    logger.debug(
        "Initializing {} thread pool with min[{}], max[{}], keep_alive[{}], capacity[{}], wait_time[{}], scheduled_size[{}]",
        getType(), min, max, keepAlive, capacity, waitTime, scheduledSize);
    executorService = DynamicExecutors.newBlockingThreadPool(min, max, keepAlive.millis(), capacity,
        waitTime.millis(), DynamicExecutors.daemonThreadFactory(settings, "[tp]"));
    scheduledExecutorService = Executors.newScheduledThreadPool(scheduledSize,
        DynamicExecutors.daemonThreadFactory(settings, "[sc]"));
    started = true;
  }

  @Override
  public String getType() {
    return "blocking";
  }
}
