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

package io.hermes.bootstrap;

import static com.google.common.collect.Sets.newHashSet;
import static io.hermes.util.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;

import com.google.inject.CreationException;
import com.google.inject.spi.Message;
import io.hermes.ExceptionsHelper;
import io.hermes.Version;
import io.hermes.env.Environment;
import io.hermes.node.Node;
import io.hermes.node.NodeBuilder;
import io.hermes.node.internal.InternalSettingsPerparer;
import io.hermes.util.Classes;
import io.hermes.util.Tuple;
import io.hermes.util.logging.HermesLogger;
import io.hermes.util.logging.Loggers;
import io.hermes.util.logging.log4j.LogConfigurator;
import io.hermes.util.settings.Settings;
import java.io.File;
import java.util.Set;

/**
 * A main entry point when starting from the command line.
 *
 * @author spancer.ray
 */
public class Bootstrap {

  private Node node;

  private static void setupLogging(Tuple<Settings, Environment> tuple) {
    try {
      Classes.getDefaultClassLoader().loadClass("org.apache.log4j.Logger");
      LogConfigurator.configure(tuple.v1());
    } catch (ClassNotFoundException e) {
      // no log4j
    } catch (NoClassDefFoundError e) {
      // no log4j
    } catch (Exception e) {
      System.err.println("Failed to configure logging...");
      e.printStackTrace();
    }
  }

  private static Tuple<Settings, Environment> initialSettings() {
    return InternalSettingsPerparer.prepareSettings(EMPTY_SETTINGS, true);
  }

  public static void main(String[] args) {
    Bootstrap bootstrap = new Bootstrap();
    String pidFile = System.getProperty("hermes-pidfile");

    boolean foreground = System.getProperty("hermes-foreground") != null;

    Tuple<Settings, Environment> tuple = null;
    try {
      tuple = initialSettings();
      setupLogging(tuple);
    } catch (Exception e) {
      String errorMessage = buildErrorMessage("Setup", e);
      System.err.println(errorMessage);
      System.err.flush();
      System.exit(3);
    }

    String stage = "Initialization";
    try {
      if (!foreground) {
        Loggers.disableConsoleLogging();
        System.out.close();
      }
      bootstrap.setup(true, tuple);

      if (pidFile != null) {
        new File(pidFile).deleteOnExit();
      }

      stage = "Startup";
      bootstrap.start();

      if (!foreground) {
        System.err.close();
      }
    } catch (Throwable e) {
      HermesLogger logger = Loggers.getLogger(Bootstrap.class);
      if (bootstrap.node != null) {
        logger = Loggers.getLogger(Bootstrap.class, bootstrap.node.settings().get("name"));
      }
      String errorMessage = buildErrorMessage(stage, e);
      if (foreground) {
        logger.error(errorMessage);
      } else {
        System.err.println(errorMessage);
        System.err.flush();
      }
      Loggers.disableConsoleLogging();
      if (logger.isDebugEnabled()) {
        logger.debug("Exception", e);
      }
      System.exit(3);
    }
  }

  private static String buildErrorMessage(String stage, Throwable e) {
    StringBuilder errorMessage = new StringBuilder("{").append(Version.full()).append("}: ");

    if (e instanceof CreationException) {
      CreationException createException = (CreationException) e;
      Set<String> seenMessages = newHashSet();
      int counter = 1;
      for (Message message : createException.getErrorMessages()) {
        String detailedMessage;
        if (message.getCause() == null) {
          detailedMessage = message.getMessage();
        } else {
          detailedMessage = ExceptionsHelper.detailedMessage(message.getCause(), true, 0);
        }
        if (detailedMessage == null) {
          detailedMessage = message.getMessage();
        }
        if (seenMessages.contains(detailedMessage)) {
          continue;
        }
        seenMessages.add(detailedMessage);
        errorMessage.append(counter++).append(") ").append(detailedMessage);
      }
    } else {
      errorMessage.append("- ").append(ExceptionsHelper.detailedMessage(e, true, 0));
    }
    return errorMessage.toString();
  }

  private void setup(boolean addShutdownHook, Tuple<Settings, Environment> tuple) throws Exception {
    NodeBuilder nodeBuilder =
        NodeBuilder.nodeBuilder().settings(tuple.v1()).loadConfigSettings(false);
    node = nodeBuilder.build();
    if (addShutdownHook) {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          node.close();
        }
      });
    }
  }

  /**
   * hook for JSVC
   */
  public void init(String[] args) throws Exception {
    Tuple<Settings, Environment> tuple = initialSettings();
    setupLogging(tuple);
    setup(true, tuple);
  }

  /**
   * hook for JSVC
   */
  public void start() {
    node.start();
  }

  /**
   * hook for JSVC
   */
  public void stop() {
    node.stop();
  }

  /**
   * hook for JSVC
   */
  public void destroy() {
    node.close();
  }
}
