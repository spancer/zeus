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

package io.hermes.monitor;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.monitor.jvm.JvmMonitorService;
import io.hermes.monitor.memory.MemoryMonitorService;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class MonitorService extends AbstractLifecycleComponent<MonitorService> {

  private final MemoryMonitorService memoryMonitorService;

  private final JvmMonitorService jvmMonitorService;

  @Inject
  public MonitorService(Settings settings, MemoryMonitorService memoryMonitorService,
      JvmMonitorService jvmMonitorService) {
    super(settings);
    this.memoryMonitorService = memoryMonitorService;
    this.jvmMonitorService = jvmMonitorService;
  }

  @Override
  protected void doStart() throws HermesException {
    memoryMonitorService.start();
    jvmMonitorService.start();
  }

  @Override
  protected void doStop() throws HermesException {
    memoryMonitorService.stop();
    jvmMonitorService.stop();
  }

  @Override
  protected void doClose() throws HermesException {
    memoryMonitorService.close();
    jvmMonitorService.close();
  }
}
