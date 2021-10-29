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

import io.hermes.HermesIllegalStateException;
import io.hermes.util.concurrent.ThreadSafe;

/**
 * Lifecycle state. Allows the following transitions:
 * <ul>
 * <li>INITIALIZED -> STARTED, STOPPED, CLOSED</li>
 * <li>STARTED -> STOPPED</li>
 * <li>STOPPED -> STARTED, CLOSED</li>
 * <li>CLOSED -></li>
 * </ul>
 *
 * <p>
 * Also allows to stay in the same state. For example, when calling stop on a component, the
 * following logic can be applied:
 *
 * <pre>
 * public void stop() {
 *   if (!lifeccycleState.moveToStopped()) {
 *     return;
 *   }
 *   // continue with stop logic
 * }
 * </pre>
 *
 * <p>
 * Note, closed is only allowed to be called when stopped, so make sure to stop the component first.
 * Here is how the logic can be applied:
 *
 * <pre>
 * public void close() {
 *   if (lifecycleState.started()) {
 *     stop();
 *   }
 *   if (!lifecycleState.moveToClosed()) {
 *     return;
 *   }
 *   // perofrm close logic here
 * }
 * </pre>
 *
 * @author spancer.ray
 */
@ThreadSafe
public class Lifecycle {

  private volatile State state = State.INITIALIZED;

  public State state() {
    return this.state;
  }

  /**
   * Returns <tt>true</tt> if the state is initialized.
   */
  public boolean initialized() {
    return state == State.INITIALIZED;
  }

  /**
   * Returns <tt>true</tt> if the state is started.
   */
  public boolean started() {
    return state == State.STARTED;
  }

  /**
   * Returns <tt>true</tt> if the state is stopped.
   */
  public boolean stopped() {
    return state == State.STOPPED;
  }

  /**
   * Returns <tt>true</tt> if the state is closed.
   */
  public boolean closed() {
    return state == State.CLOSED;
  }

  public boolean canMoveToStarted() throws HermesIllegalStateException {
    State localState = this.state;
    if (localState == State.INITIALIZED || localState == State.STOPPED) {
      return true;
    }
    if (localState == State.STARTED) {
      return false;
    }
    if (localState == State.CLOSED) {
      throw new HermesIllegalStateException("Can't move to started state when closed");
    }
    throw new HermesIllegalStateException("Can't move to started with unknown state");
  }

  public boolean moveToStarted() throws HermesIllegalStateException {
    State localState = this.state;
    if (localState == State.INITIALIZED || localState == State.STOPPED) {
      state = State.STARTED;
      return true;
    }
    if (localState == State.STARTED) {
      return false;
    }
    if (localState == State.CLOSED) {
      throw new HermesIllegalStateException("Can't move to started state when closed");
    }
    throw new HermesIllegalStateException("Can't move to started with unknown state");
  }

  public boolean canMoveToStopped() throws HermesIllegalStateException {
    State localState = state;
    if (localState == State.STARTED) {
      return true;
    }
    if (localState == State.INITIALIZED || localState == State.STOPPED) {
      return false;
    }
    if (localState == State.CLOSED) {
      throw new HermesIllegalStateException("Can't move to started state when closed");
    }
    throw new HermesIllegalStateException("Can't move to started with unknown state");
  }

  public boolean moveToStopped() throws HermesIllegalStateException {
    State localState = state;
    if (localState == State.STARTED) {
      state = State.STOPPED;
      return true;
    }
    if (localState == State.INITIALIZED || localState == State.STOPPED) {
      return false;
    }
    if (localState == State.CLOSED) {
      throw new HermesIllegalStateException("Can't move to started state when closed");
    }
    throw new HermesIllegalStateException("Can't move to started with unknown state");
  }

  public boolean canMoveToClosed() throws HermesIllegalStateException {
    State localState = state;
    if (localState == State.CLOSED) {
      return false;
    }
    if (localState == State.STARTED) {
      throw new HermesIllegalStateException("Can't move to closed before moving to stopped mode");
    }
    return true;
  }

  public boolean moveToClosed() throws HermesIllegalStateException {
    State localState = state;
    if (localState == State.CLOSED) {
      return false;
    }
    if (localState == State.STARTED) {
      throw new HermesIllegalStateException("Can't move to closed before moving to stopped mode");
    }
    state = State.CLOSED;
    return true;
  }

  @Override
  public String toString() {
    return state.toString();
  }

  public enum State {
    INITIALIZED, STOPPED, STARTED, CLOSED
  }
}
