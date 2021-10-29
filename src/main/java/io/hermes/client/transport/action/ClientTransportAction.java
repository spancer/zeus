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


package io.hermes.client.transport.action;

import io.hermes.HermesException;
import io.hermes.action.ActionFuture;
import io.hermes.action.ActionListener;
import io.hermes.action.ActionRequest;
import io.hermes.action.ActionResponse;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.util.Nullable;


public interface ClientTransportAction<Request extends ActionRequest, Response extends ActionResponse> {

  ActionFuture<Response> submit(DiscoveryNode node, Request request) throws HermesException;

  ActionFuture<Response> submit(DiscoveryNode node, Request request,
      @Nullable ActionListener<Response> listener);

  void execute(DiscoveryNode node, Request request, ActionListener<Response> listener);
}
