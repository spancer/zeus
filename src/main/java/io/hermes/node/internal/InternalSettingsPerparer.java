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

package io.hermes.node.internal;

import static io.hermes.util.Strings.cleanPath;
import static io.hermes.util.settings.ImmutableSettings.settingsBuilder;

import io.hermes.env.Environment;
import io.hermes.env.FailedToResolveConfigException;
import io.hermes.util.Names;
import io.hermes.util.Tuple;
import io.hermes.util.settings.ImmutableSettings;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class InternalSettingsPerparer {

  public static Tuple<Settings, Environment> prepareSettings(Settings pSettings,
      boolean loadConfigSettings) {
    // just create enough settings to build the environment
    ImmutableSettings.Builder settingsBuilder = settingsBuilder().put(pSettings)
        .putProperties("hermes.", System.getProperties()).replacePropertyPlaceholders();

    Environment environment = new Environment(settingsBuilder.build());

    // put back the env settings
    settingsBuilder = settingsBuilder().put(pSettings);
    settingsBuilder.put("path.home", cleanPath(environment.homeFile().getAbsolutePath()));
    settingsBuilder.put("path.work", cleanPath(environment.workFile().getAbsolutePath()));
    settingsBuilder.put("path.scripts", cleanPath(environment.scriptsFile().getAbsolutePath()));
    settingsBuilder.put("path.logs", cleanPath(environment.logsFile().getAbsolutePath()));

    if (loadConfigSettings) {
      try {
        settingsBuilder.loadFromUrl(environment.resolveConfig("hermes.yml"));
      } catch (FailedToResolveConfigException e) {
        // ignore
      } catch (NoClassDefFoundError e) {
        // ignore, no yaml
      }
      try {
        settingsBuilder.loadFromUrl(environment.resolveConfig("hermes.properties"));
      } catch (FailedToResolveConfigException e) {
        // ignore
      }
      if (System.getProperty("hermes.config") != null) {
        settingsBuilder.loadFromUrl(environment.resolveConfig(System.getProperty("hermes.config")));
      }
    }

    settingsBuilder.put(pSettings).putProperties("hermes.", System.getProperties())
        .replacePropertyPlaceholders();

    // generate the name
    if (settingsBuilder.get("name") == null) {
      String name = System.getProperty("name");
      if (name == null || name.isEmpty()) {
        name = Names.randomNodeName(environment.resolveConfig("names.txt"));
      }

      if (name != null) {
        settingsBuilder.put("name", name);
      }
    }

    return new Tuple<Settings, Environment>(settingsBuilder.build(), environment);
  }

}
