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

package io.hermes.transport;

import io.hermes.HermesException;
import io.hermes.HermesInterruptedException;
import io.hermes.util.concurrent.AbstractFuture;
import io.hermes.util.io.stream.Streamable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author spancer.ray
 */
public class PlainTransportFuture<V extends Streamable> extends AbstractFuture<V>
    implements TransportFuture<V>, TransportResponseHandler<V> {

  private final TransportResponseHandler<V> handler;

  public PlainTransportFuture(TransportResponseHandler<V> handler) {
    this.handler = handler;
  }

  @Override
  public V txGet() throws HermesException {
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
  public V txGet(long timeout, TimeUnit unit) throws HermesException, TimeoutException {
    try {
      return get(timeout, unit);
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
  public V newInstance() {
    return handler.newInstance();
  }

  @Override
  public void handleResponse(V response) {
    handler.handleResponse(response);
    set(response);
  }

  @Override
  public void handleException(RemoteTransportException exp) {
    handler.handleException(exp);
    setException(exp);
  }

  @Override
  public boolean spawn() {
    return handler.spawn();
  }
}
