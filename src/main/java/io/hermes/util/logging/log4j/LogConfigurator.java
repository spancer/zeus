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

package io.hermes.util.logging.log4j;

import static io.hermes.util.settings.ImmutableSettings.settingsBuilder;

import com.google.common.collect.ImmutableMap;
import io.hermes.env.Environment;
import io.hermes.env.FailedToResolveConfigException;
import io.hermes.util.MapBuilder;
import io.hermes.util.settings.ImmutableSettings;
import io.hermes.util.settings.Settings;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author spancer.ray
 */
public class LogConfigurator {

  private static boolean loaded;

  private static ImmutableMap<String, String> replacements = new MapBuilder<String, String>()
      .put("console", "io.hermes.util.logging.log4j.ConsoleAppender")
      .put("async", "org.apache.log4j.AsyncAppender")
      .put("dailyRollingFile", "org.apache.log4j.DailyRollingFileAppender")
      .put("externallyRolledFile", "org.apache.log4j.ExternallyRolledFileAppender")
      .put("file", "org.apache.log4j.FileAppender").put("jdbc", "org.apache.log4j.JDBCAppender")
      .put("jms", "org.apache.log4j.JMSAppender").put("lf5", "org.apache.log4j.LF5Appender")
      .put("ntevent", "org.apache.log4j.NTEventLogAppender")
      .put("null", "org.apache.log4j.NullAppender")
      .put("rollingFile", "org.apache.log4j.RollingFileAppender")
      .put("smtp", "org.apache.log4j.SMTPAppender").put("socket", "org.apache.log4j.SocketAppender")
      .put("socketHub", "org.apache.log4j.SocketHubAppender")
      .put("syslog", "org.apache.log4j.SyslogAppender")
      .put("telnet", "org.apache.log4j.TelnetAppender")
      // layouts
      .put("simple", "org.apache.log4j.SimpleLayout").put("html", "org.apache.log4j.HTMLLayout")
      .put("pattern", "org.apache.log4j.PatternLayout").put("ttcc", "org.apache.log4j.TTCCLayout")
      .put("xml", "org.apache.log4j.XMLLayout").immutableMap();

  public static void configure(Settings settings) {
    if (loaded) {
      return;
    }
    loaded = true;
    Environment environment = new Environment(settings);
    ImmutableSettings.Builder settingsBuilder = settingsBuilder().put(settings);
    try {
      settingsBuilder.loadFromUrl(environment.resolveConfig("logging.yml"));
    } catch (FailedToResolveConfigException e) {
      // ignore
    } catch (NoClassDefFoundError e) {
      // ignore, no yaml
    }
    try {
      settingsBuilder.loadFromUrl(environment.resolveConfig("logging.json"));
    } catch (FailedToResolveConfigException e) {
      // ignore
    }
    try {
      settingsBuilder.loadFromUrl(environment.resolveConfig("logging.properties"));
    } catch (FailedToResolveConfigException e) {
      // ignore
    }
    settingsBuilder.putProperties("hermes.", System.getProperties()).replacePropertyPlaceholders();
    Properties props = new Properties();
    for (Map.Entry<String, String> entry : settingsBuilder.build().getAsMap().entrySet()) {
      String key = "log4j." + entry.getKey();
      String value = entry.getValue();
      if (replacements.containsKey(value)) {
        value = replacements.get(value);
      }
      if (key.endsWith(".value")) {
        props.setProperty(key.substring(0, key.length() - ".value".length()), value);
      } else if (key.endsWith(".type")) {
        props.setProperty(key.substring(0, key.length() - ".type".length()), value);
      } else {
        props.setProperty(key, value);
      }
    }
    PropertyConfigurator.configure(props);
  }
}
