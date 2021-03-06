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

import io.hermes.HermesException;
import io.hermes.util.settings.Settings;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author spancer.ray
 */
public abstract class AbstractLifecycleComponent<T> extends AbstractComponent
    implements LifecycleComponent<T> {

  protected final Lifecycle lifecycle = new Lifecycle();

  private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<LifecycleListener>();

  protected AbstractLifecycleComponent(Settings settings) {
    super(settings);
  }

  protected AbstractLifecycleComponent(Settings settings, Class customClass) {
    super(settings, customClass);
  }

  protected AbstractLifecycleComponent(Settings settings, Class loggerClass, Class componentClass) {
    super(settings, loggerClass, componentClass);
  }

  protected AbstractLifecycleComponent(Settings settings, String prefixSettings) {
    super(settings, prefixSettings);
  }

  protected AbstractLifecycleComponent(Settings settings, String prefixSettings,
      Class customClass) {
    super(settings, prefixSettings, customClass);
  }

  protected AbstractLifecycleComponent(Settings settings, String prefixSettings, Class loggerClass,
      Class componentClass) {
    super(settings, prefixSettings, loggerClass, componentClass);
  }

  @Override
  public Lifecycle.State lifecycleState() {
    return this.lifecycle.state();
  }

  @Override
  public void addLifecycleListener(LifecycleListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeLifecycleListener(LifecycleListener listener) {
    listeners.remove(listener);
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public T start() throws HermesException {
    if (!lifecycle.canMoveToStarted()) {
      return (T) this;
    }
    for (LifecycleListener listener : listeners) {
      listener.beforeStart();
    }
    doStart();
    lifecycle.moveToStarted();
    for (LifecycleListener listener : listeners) {
      listener.afterStart();
    }
    return (T) this;
  }

  protected abstract void doStart() throws HermesException;

  @SuppressWarnings({"unchecked"})
  @Override
  public T stop() throws HermesException {
    if (!lifecycle.canMoveToStopped()) {
      return (T) this;
    }
    for (LifecycleListener listener : listeners) {
      listener.beforeStop();
    }
    lifecycle.moveToStopped();
    doStop();
    for (LifecycleListener listener : listeners) {
      listener.afterStop();
    }
    return (T) this;
  }

  protected abstract void doStop() throws HermesException;

  @Override
  public void close() throws HermesException {
    if (lifecycle.started()) {
      stop();
    }
    if (!lifecycle.canMoveToClosed()) {
      return;
    }
    for (LifecycleListener listener : listeners) {
      listener.beforeClose();
    }
    lifecycle.moveToClosed();
    doClose();
    for (LifecycleListener listener : listeners) {
      listener.afterClose();
    }
  }

  protected abstract void doClose() throws HermesException;
}
