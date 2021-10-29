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

package io.hermes.util.logging.jdk;

import io.hermes.util.logging.support.AbstractHermesLogger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author spancer.ray
 */
public class JdkHermesLogger extends AbstractHermesLogger {

  private final Logger logger;

  public JdkHermesLogger(String prefix, Logger logger) {
    super(prefix);
    this.logger = logger;
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isLoggable(Level.FINEST);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isLoggable(Level.FINE);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isLoggable(Level.INFO);
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isLoggable(Level.WARNING);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isLoggable(Level.SEVERE);
  }

  @Override
  protected void internalTrace(String msg) {
    logger.log(Level.FINEST, msg);
  }

  @Override
  protected void internalTrace(String msg, Throwable cause) {
    logger.log(Level.FINEST, msg, cause);
  }

  @Override
  protected void internalDebug(String msg) {
    logger.log(Level.FINE, msg);
  }

  @Override
  protected void internalDebug(String msg, Throwable cause) {
    logger.log(Level.FINE, msg, cause);
  }

  @Override
  protected void internalInfo(String msg) {
    logger.log(Level.INFO, msg);
  }

  @Override
  protected void internalInfo(String msg, Throwable cause) {
    logger.log(Level.INFO, msg, cause);
  }

  @Override
  protected void internalWarn(String msg) {
    logger.log(Level.WARNING, msg);
  }

  @Override
  protected void internalWarn(String msg, Throwable cause) {
    logger.log(Level.WARNING, msg, cause);
  }

  @Override
  protected void internalError(String msg) {
    logger.log(Level.SEVERE, msg);
  }

  @Override
  protected void internalError(String msg, Throwable cause) {
    logger.log(Level.SEVERE, msg, cause);
  }
}
