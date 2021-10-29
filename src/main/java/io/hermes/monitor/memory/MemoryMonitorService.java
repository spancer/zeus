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

package io.hermes.monitor.memory;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class MemoryMonitorService extends AbstractLifecycleComponent<MemoryMonitorService> {

  private final MemoryMonitor memoryMonitor;

  @Inject
  public MemoryMonitorService(Settings settings, MemoryMonitor memoryMonitor) {
    super(settings);
    this.memoryMonitor = memoryMonitor;
  }

  @Override
  protected void doStart() throws HermesException {
    memoryMonitor.start();
  }

  @Override
  protected void doStop() throws HermesException {
    memoryMonitor.stop();
  }

  @Override
  protected void doClose() throws HermesException {
    memoryMonitor.close();
  }
}
