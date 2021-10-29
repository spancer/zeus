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

package io.hermes.util.component;

import io.hermes.util.logging.HermesLogger;
import io.hermes.util.logging.Loggers;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class AbstractComponent {

  protected final HermesLogger logger;

  protected final Settings settings;

  protected final Settings componentSettings;

  public AbstractComponent(Settings settings) {
    this.logger = Loggers.getLogger(getClass(), settings);
    this.settings = settings;
    this.componentSettings = settings.getComponentSettings(getClass());
  }

  public AbstractComponent(Settings settings, String prefixSettings) {
    this.logger = Loggers.getLogger(getClass(), settings);
    this.settings = settings;
    this.componentSettings = settings.getComponentSettings(prefixSettings, getClass());
  }

  public AbstractComponent(Settings settings, Class customClass) {
    this.logger = Loggers.getLogger(customClass, settings);
    this.settings = settings;
    this.componentSettings = settings.getComponentSettings(customClass);
  }

  public AbstractComponent(Settings settings, String prefixSettings, Class customClass) {
    this.logger = Loggers.getLogger(customClass, settings);
    this.settings = settings;
    this.componentSettings = settings.getComponentSettings(prefixSettings, customClass);
  }

  public AbstractComponent(Settings settings, Class loggerClass, Class componentClass) {
    this.logger = Loggers.getLogger(loggerClass, settings);
    this.settings = settings;
    this.componentSettings = settings.getComponentSettings(componentClass);
  }

  public AbstractComponent(Settings settings, String prefixSettings, Class loggerClass,
      Class componentClass) {
    this.logger = Loggers.getLogger(loggerClass, settings);
    this.settings = settings;
    this.componentSettings = settings.getComponentSettings(prefixSettings, componentClass);
  }

  public String nodeName() {
    return settings.get("name", "");
  }
}
