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

package io.hermes.action.support.broadcast;

import io.hermes.action.ActionListener;
import io.hermes.action.support.BaseAction;
import io.hermes.cluster.ClusterService;
import io.hermes.cluster.ClusterState;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.BaseTransportRequestHandler;
import io.hermes.transport.TransportChannel;
import io.hermes.transport.TransportService;
import io.hermes.util.settings.Settings;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * TODO indices needs to be
 *
 * @author spancer.ray
 */
public abstract class TransportBroadcastOperationAction<Request extends BroadcastOperationRequest, Response extends BroadcastOperationResponse>
    extends BaseAction<Request, Response> {

  protected final ClusterService clusterService;

  protected final TransportService transportService;

//  protected final IndicesService indicesService;

  protected final ThreadPool threadPool;

  protected TransportBroadcastOperationAction(Settings settings, ThreadPool threadPool,
      ClusterService clusterService, TransportService transportService) {
    super(settings);
    this.clusterService = clusterService;
    this.transportService = transportService;
    this.threadPool = threadPool;

    transportService.registerHandler(transportAction(), new TransportHandler());
  }

  @Override
  protected void doExecute(Request request, ActionListener<Response> listener) {
//    new AsyncBroadcastAction(request, listener).start();
  }

  protected abstract String transportAction();

  protected abstract String transportShardAction();

  protected abstract Request newRequest();

  protected abstract Response newResponse(Request request, AtomicReferenceArray shardsResponses,
      ClusterState clusterState);


  protected boolean accumulateExceptions() {
    return true;
  }

  protected boolean ignoreNonActiveExceptions() {
    return false;
  }

  class AsyncBroadcastAction {

    private final Request request;

    private final ActionListener<Response> listener;

    private final ClusterState clusterState;

    private final DiscoveryNodes nodes;


    private final AtomicInteger counterOps = new AtomicInteger();

    private final AtomicInteger indexCounter = new AtomicInteger();


    AsyncBroadcastAction(Request request, ActionListener<Response> listener) {
      this.request = request;
      this.listener = listener;

      clusterState = clusterService.state();

      nodes = clusterState.nodes();

    }


  }

  class TransportHandler extends BaseTransportRequestHandler<Request> {

    @Override
    public Request newInstance() {
      return newRequest();
    }

    @Override
    public void messageReceived(Request request, final TransportChannel channel) throws Exception {
      // we just send back a response, no need to fork a listener
      request.listenerThreaded(false);
      // we don't spawn, so if we get a request with no threading, change it to single threaded
      if (request.operationThreading() == BroadcastOperationThreading.NO_THREADS) {
        request.operationThreading(BroadcastOperationThreading.SINGLE_THREAD);
      }
      execute(request, new ActionListener<Response>() {
        @Override
        public void onResponse(Response response) {
          try {
            channel.sendResponse(response);
          } catch (Exception e) {
            onFailure(e);
          }
        }

        @Override
        public void onFailure(Throwable e) {
          try {
            channel.sendResponse(e);
          } catch (Exception e1) {
            logger.warn("Failed to send response", e1);
          }
        }
      });
    }

    @Override
    public boolean spawn() {
      return false;
    }
  }

}
