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

package io.hermes.util.logging.support;

import io.hermes.util.logging.HermesLogger;

/**
 * @author spancer.ray
 */
public abstract class AbstractHermesLogger implements HermesLogger {

  private final String prefix;

  protected AbstractHermesLogger(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String getPrefix() {
    return this.prefix;
  }

  @Override
  public void trace(String msg, Object... params) {
    if (isTraceEnabled()) {
      internalTrace(LoggerMessageFormat.format(prefix, msg, params));
    }
  }

  protected abstract void internalTrace(String msg);

  @Override
  public void trace(String msg, Throwable cause, Object... params) {
    if (isTraceEnabled()) {
      internalTrace(LoggerMessageFormat.format(prefix, msg, params), cause);
    }
  }

  protected abstract void internalTrace(String msg, Throwable cause);


  @Override
  public void debug(String msg, Object... params) {
    if (isDebugEnabled()) {
      internalDebug(LoggerMessageFormat.format(prefix, msg, params));
    }
  }

  protected abstract void internalDebug(String msg);

  @Override
  public void debug(String msg, Throwable cause, Object... params) {
    if (isDebugEnabled()) {
      internalDebug(LoggerMessageFormat.format(prefix, msg, params), cause);
    }
  }

  protected abstract void internalDebug(String msg, Throwable cause);


  @Override
  public void info(String msg, Object... params) {
    if (isInfoEnabled()) {
      internalInfo(LoggerMessageFormat.format(prefix, msg, params));
    }
  }

  protected abstract void internalInfo(String msg);

  @Override
  public void info(String msg, Throwable cause, Object... params) {
    if (isInfoEnabled()) {
      internalInfo(LoggerMessageFormat.format(prefix, msg, params), cause);
    }
  }

  protected abstract void internalInfo(String msg, Throwable cause);


  @Override
  public void warn(String msg, Object... params) {
    if (isWarnEnabled()) {
      internalWarn(LoggerMessageFormat.format(prefix, msg, params));
    }
  }

  protected abstract void internalWarn(String msg);

  @Override
  public void warn(String msg, Throwable cause, Object... params) {
    if (isWarnEnabled()) {
      internalWarn(LoggerMessageFormat.format(prefix, msg, params), cause);
    }
  }

  protected abstract void internalWarn(String msg, Throwable cause);


  @Override
  public void error(String msg, Object... params) {
    if (isErrorEnabled()) {
      internalError(LoggerMessageFormat.format(prefix, msg, params));
    }
  }

  protected abstract void internalError(String msg);

  @Override
  public void error(String msg, Throwable cause, Object... params) {
    if (isErrorEnabled()) {
      internalError(LoggerMessageFormat.format(prefix, msg, params), cause);
    }
  }

  protected abstract void internalError(String msg, Throwable cause);
}
