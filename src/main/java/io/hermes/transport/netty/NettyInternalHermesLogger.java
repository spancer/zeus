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

package io.hermes.transport.netty;

import io.hermes.util.logging.HermesLogger;
import io.netty.util.internal.logging.AbstractInternalLogger;

/**
 * @author spancer.ray
 */
public class NettyInternalHermesLogger extends AbstractInternalLogger {

  private final HermesLogger logger;

  public NettyInternalHermesLogger(String name, HermesLogger logger) {
    super(name);
    this.logger = logger;
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
    return logger.isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public void debug(String msg) {
    logger.debug(msg);
  }

  @Override
  public void debug(String msg, Throwable cause) {
    logger.debug(msg, cause);
  }

  @Override
  public void info(String msg) {
    logger.info(msg);
  }

  @Override
  public void info(String msg, Throwable cause) {
    logger.info(msg, cause);
  }

  @Override
  public void warn(String msg) {
    logger.warn(msg);
  }

  @Override
  public void warn(String msg, Throwable cause) {
    logger.warn(msg, cause);
  }

  @Override
  public void error(String msg) {
    logger.error(msg);
  }

  @Override
  public void error(String msg, Throwable cause) {
    logger.error(msg, cause);
  }

  @Override
  public boolean isTraceEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void trace(String msg) {
    // TODO Auto-generated method stub

  }

  @Override
  public void trace(String format, Object arg) {
    // TODO Auto-generated method stub

  }

  @Override
  public void trace(String format, Object argA, Object argB) {
    // TODO Auto-generated method stub

  }

  @Override
  public void trace(String format, Object... arguments) {
    // TODO Auto-generated method stub

  }

  @Override
  public void trace(String msg, Throwable t) {
    // TODO Auto-generated method stub

  }

  @Override
  public void debug(String format, Object arg) {
    // TODO Auto-generated method stub

  }

  @Override
  public void debug(String format, Object argA, Object argB) {
    // TODO Auto-generated method stub

  }

  @Override
  public void debug(String format, Object... arguments) {
    // TODO Auto-generated method stub

  }

  @Override
  public void info(String format, Object arg) {
    // TODO Auto-generated method stub

  }

  @Override
  public void info(String format, Object argA, Object argB) {
    // TODO Auto-generated method stub

  }

  @Override
  public void info(String format, Object... arguments) {
    // TODO Auto-generated method stub

  }

  @Override
  public void warn(String format, Object arg) {
    // TODO Auto-generated method stub

  }

  @Override
  public void warn(String format, Object... arguments) {
    // TODO Auto-generated method stub

  }

  @Override
  public void warn(String format, Object argA, Object argB) {
    // TODO Auto-generated method stub

  }

  @Override
  public void error(String format, Object arg) {
    // TODO Auto-generated method stub

  }

  @Override
  public void error(String format, Object argA, Object argB) {
    // TODO Auto-generated method stub

  }

  @Override
  public void error(String format, Object... arguments) {
    // TODO Auto-generated method stub

  }
}
