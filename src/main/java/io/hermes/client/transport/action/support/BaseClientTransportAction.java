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


package io.hermes.client.transport.action.support;


import static io.hermes.action.support.PlainActionFuture.newFuture;

import io.hermes.HermesException;
import io.hermes.HermesIllegalArgumentException;
import io.hermes.HermesIllegalStateException;
import io.hermes.action.ActionFuture;
import io.hermes.action.ActionListener;
import io.hermes.action.ActionRequest;
import io.hermes.action.ActionResponse;
import io.hermes.action.support.PlainActionFuture;
import io.hermes.client.transport.action.ClientTransportAction;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.transport.BaseTransportResponseHandler;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.TransportService;
import io.hermes.util.Nullable;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.logging.HermesLogger;
import io.hermes.util.logging.Loggers;
import io.hermes.util.settings.Settings;
import java.lang.reflect.Constructor;


public abstract class BaseClientTransportAction<Request extends ActionRequest, Response extends ActionResponse> extends
    AbstractComponent implements ClientTransportAction<Request, Response> {

  protected final TransportService transportService;

  private final Constructor<Response> responseConstructor;

  private final HermesLogger logger = Loggers.getLogger(BaseClientTransportAction.class);

  protected BaseClientTransportAction(Settings settings, TransportService transportService,
      Class<Response> type) {
    super(settings);
    this.transportService = transportService;
    try {
      this.responseConstructor = type.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      throw new HermesIllegalArgumentException(
          "No default constructor is declared for [" + type.getName() + "]");
    }
    responseConstructor.setAccessible(true);
  }

  @Override
  public ActionFuture<Response> submit(DiscoveryNode node, Request request) throws HermesException {
    return submit(node, request, null);
  }

  @Override
  public ActionFuture<Response> submit(DiscoveryNode node, Request request,
      @Nullable ActionListener<Response> listener) {
    PlainActionFuture<Response> future = newFuture();
    if (listener == null) {
      // since we don't have a listener, and we release a possible lock with the future
      // there is no need to execute it under a listener thread
      request.listenerThreaded(false);
    }
    execute(node, request, future);
    return future;
  }


  @Override
  public void execute(DiscoveryNode node, final Request request,
      final ActionListener<Response> listener) {
    logger.info("current request action for client: {}", action());
    transportService
        .sendRequest(node, action(), request, new BaseTransportResponseHandler<Response>() {
          @Override
          public Response newInstance() {
            return BaseClientTransportAction.this.newInstance();
          }

          @Override
          public void handleResponse(Response response) {
            listener.onResponse(response);
          }

          @Override
          public void handleException(RemoteTransportException exp) {
            listener.onFailure(exp);
          }

          @Override
          public boolean spawn() {
            return request.listenerThreaded();
          }
        });
  }

  protected abstract String action();

  protected Response newInstance() {
    try {
      return responseConstructor.newInstance();
    } catch (Exception e) {
      throw new HermesIllegalStateException("Failed to create a new instance");
    }
  }
}
