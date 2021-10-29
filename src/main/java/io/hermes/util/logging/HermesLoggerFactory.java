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

package io.hermes.util.logging;

import io.hermes.util.logging.jdk.JdkHermesLoggerFactory;
import io.hermes.util.logging.log4j.Log4jHermesLoggerFactory;
import io.hermes.util.logging.slf4j.Slf4jHermesLoggerFactory;

/**
 * @author spancer.ray
 */
public abstract class HermesLoggerFactory {

  private static volatile HermesLoggerFactory defaultFactory = new JdkHermesLoggerFactory();

  static {
    try {
      Class.forName("org.slf4j.Logger");
      defaultFactory = new Slf4jHermesLoggerFactory();
    } catch (Throwable e) {
      // no slf4j
      try {
        Class.forName("org.apache.log4j.Logger");
        defaultFactory = new Log4jHermesLoggerFactory();
      } catch (Throwable e1) {
        // no log4j
      }
    }
  }

  /**
   * Changes the default factory.
   */
  public static void setDefaultFactory(HermesLoggerFactory defaultFactory) {
    if (defaultFactory == null) {
      throw new NullPointerException("defaultFactory");
    }
    HermesLoggerFactory.defaultFactory = defaultFactory;
  }


  public static HermesLogger getLogger(String prefix, String name) {
    return defaultFactory.newInstance(prefix, name);
  }

  public static HermesLogger getLogger(String name) {
    return defaultFactory.newInstance(name);
  }

  public HermesLogger newInstance(String name) {
    return newInstance(null, name);
  }

  public abstract HermesLogger newInstance(String prefix, String name);
}
