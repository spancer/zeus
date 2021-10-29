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

import io.hermes.util.logging.support.AbstractHermesLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author spancer.ray
 */
public class Log4jHermesLogger extends AbstractHermesLogger {

  private final org.apache.log4j.Logger logger;

  public Log4jHermesLogger(String prefix, Logger logger) {
    super(prefix);
    this.logger = logger;
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isEnabledFor(Level.WARN);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isEnabledFor(Level.ERROR);
  }

  @Override
  protected void internalTrace(String msg) {
    logger.trace(msg);
  }

  @Override
  protected void internalTrace(String msg, Throwable cause) {
    logger.trace(msg, cause);
  }

  @Override
  protected void internalDebug(String msg) {
    logger.debug(msg);
  }

  @Override
  protected void internalDebug(String msg, Throwable cause) {
    logger.debug(msg, cause);
  }

  @Override
  protected void internalInfo(String msg) {
    logger.info(msg);
  }

  @Override
  protected void internalInfo(String msg, Throwable cause) {
    logger.info(msg, cause);
  }

  @Override
  protected void internalWarn(String msg) {
    logger.warn(msg);
  }

  @Override
  protected void internalWarn(String msg, Throwable cause) {
    logger.warn(msg, cause);
  }

  @Override
  protected void internalError(String msg) {
    logger.error(msg);
  }

  @Override
  protected void internalError(String msg, Throwable cause) {
    logger.error(msg, cause);
  }
}
