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

package io.hermes.action.support;

import io.hermes.HermesException;
import io.hermes.HermesInterruptedException;
import io.hermes.HermesTimeoutException;
import io.hermes.action.ActionFuture;
import io.hermes.action.ActionListener;
import io.hermes.transport.TransportException;
import io.hermes.util.TimeValue;
import io.hermes.util.concurrent.AbstractFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author spancer.ray
 */
public abstract class AdapterActionFuture<T, L> extends AbstractFuture<T> implements
    ActionFuture<T>, ActionListener<L> {

  @Override
  public T actionGet() throws HermesException {
    try {
      return get();
    } catch (InterruptedException e) {
      throw new HermesInterruptedException(e.getMessage());
    } catch (ExecutionException e) {
      if (e.getCause() instanceof HermesException) {
        throw (HermesException) e.getCause();
      } else {
        throw new TransportException("Failed execution", e);
      }
    }
  }

  @Override
  public T actionGet(String timeout) throws HermesException {
    return actionGet(TimeValue.parseTimeValue(timeout, null));
  }

  @Override
  public T actionGet(long timeoutMillis) throws HermesException {
    return actionGet(timeoutMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public T actionGet(TimeValue timeout) throws HermesException {
    return actionGet(timeout.millis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public T actionGet(long timeout, TimeUnit unit) throws HermesException {
    try {
      return get(timeout, unit);
    } catch (TimeoutException e) {
      throw new HermesTimeoutException(e.getMessage());
    } catch (InterruptedException e) {
      throw new HermesInterruptedException(e.getMessage());
    } catch (ExecutionException e) {
      if (e.getCause() instanceof HermesException) {
        throw (HermesException) e.getCause();
      } else {
        throw new HermesException("Failed execution", e);
      }
    }
  }

  @Override
  public void onResponse(L result) {
    set(convert(result));
  }

  @Override
  public void onFailure(Throwable e) {
    setException(e);
  }

  protected abstract T convert(L listenerResponse);
}
