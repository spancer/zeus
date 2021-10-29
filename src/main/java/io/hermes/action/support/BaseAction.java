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

import static io.hermes.action.support.PlainActionFuture.newFuture;

import io.hermes.HermesException;
import io.hermes.action.Action;
import io.hermes.action.ActionFuture;
import io.hermes.action.ActionListener;
import io.hermes.action.ActionRequest;
import io.hermes.action.ActionRequestValidationException;
import io.hermes.action.ActionResponse;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public abstract class BaseAction<Request extends ActionRequest, Response extends ActionResponse> extends
    AbstractComponent implements Action<Request, Response> {

  protected BaseAction(Settings settings) {
    super(settings);
  }

  @Override
  public ActionFuture<Response> execute(Request request) throws HermesException {
    PlainActionFuture<Response> future = newFuture();
    // since we don't have a listener, and we release a possible lock with the future
    // there is no need to execute it under a listener thread
    request.listenerThreaded(false);
    execute(request, future);
    return future;
  }

  @Override
  public void execute(Request request, ActionListener<Response> listener) {
    ActionRequestValidationException validationException = request.validate();
    if (validationException != null) {
      listener.onFailure(validationException);
      return;
    }
    try {
      doExecute(request, listener);
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }

  protected abstract void doExecute(Request request, ActionListener<Response> listener);
}
