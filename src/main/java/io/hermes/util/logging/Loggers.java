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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

import com.google.common.collect.Lists;
import io.hermes.lakehouse.LakeHouse;
import io.hermes.util.Classes;
import io.hermes.util.settings.Settings;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * A set of utilities around Logging.
 *
 * @author spancer.ray
 */
public class Loggers {

  private static boolean consoleLoggingEnabled = true;

  public static void disableConsoleLogging() {
    consoleLoggingEnabled = false;
  }

  public static void enableConsoleLogging() {
    consoleLoggingEnabled = true;
  }

  public static boolean consoleLoggingEnabled() {
    return consoleLoggingEnabled;
  }


  public static HermesLogger getLogger(Class clazz, Settings settings, LakeHouse lakeHouse,
      String... prefixes) {
    return getLogger(clazz, settings,
        Lists.asList(lakeHouse.name(), prefixes).toArray(new String[0]));
  }

  public static HermesLogger getLogger(Class clazz, Settings settings, String... prefixes) {
    List<String> prefixesList = newArrayList();
    if (settings.getAsBoolean("logger.logHostAddress", false)) {
      try {
        prefixesList.add(InetAddress.getLocalHost().getHostAddress());
      } catch (UnknownHostException e) {
        // ignore
      }
    }
    if (settings.getAsBoolean("logger.logHostName", false)) {
      try {
        prefixesList.add(InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException e) {
        // ignore
      }
    }
    String name = settings.get("name");
    if (name != null) {
      prefixesList.add(name);
    }
    if (prefixes != null && prefixes.length > 0) {
      prefixesList.addAll(asList(prefixes));
    }
    return getLogger(getLoggerName(clazz), prefixesList.toArray(new String[prefixesList.size()]));
  }

  public static HermesLogger getLogger(HermesLogger parentLogger, String s) {
    return getLogger(parentLogger.getName() + s, parentLogger.getPrefix());
  }

  public static HermesLogger getLogger(String s) {
    return HermesLoggerFactory.getLogger(s);
  }

  public static HermesLogger getLogger(Class clazz) {
    return HermesLoggerFactory.getLogger(getLoggerName(clazz));
  }

  public static HermesLogger getLogger(Class clazz, String... prefixes) {
    return getLogger(getLoggerName(clazz), prefixes);
  }

  public static HermesLogger getLogger(String name, String... prefixes) {
    String prefix = null;
    if (prefixes != null && prefixes.length > 0) {
      StringBuilder sb = new StringBuilder();
      for (String prefixX : prefixes) {
        if (prefixX != null) {
          sb.append("[").append(prefixX).append("]");
        }
      }
      if (sb.length() > 0) {
        sb.append(" ");
        prefix = sb.toString();
      }
    }
    return HermesLoggerFactory.getLogger(prefix, getLoggerName(name));
  }

  private static String getLoggerName(Class clazz) {
    String name = clazz.getName();
    if (name.startsWith("io.hermes.")) {
      name = Classes.getPackageName(clazz);
    }
    return getLoggerName(name);
  }

  private static String getLoggerName(String name) {
    if (name.startsWith("io.hermes.")) {
      return name.substring("io.hermes.".length());
    }
    return name;
  }
}
