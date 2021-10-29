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

package io.hermes.action.support.master;

import io.hermes.HermesException;
import io.hermes.action.ActionListener;
import io.hermes.action.ActionResponse;
import io.hermes.action.support.BaseAction;
import io.hermes.cluster.ClusterService;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.BaseTransportRequestHandler;
import io.hermes.transport.BaseTransportResponseHandler;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.TransportChannel;
import io.hermes.transport.TransportService;
import io.hermes.util.settings.Settings;

/**
 * A base class for operations that needs to be performed on the master node.
 *
 * @author spancer.ray
 */
public abstract class TransportMasterNodeOperationAction<Request extends MasterNodeOperationRequest, Response extends ActionResponse>
    extends BaseAction<Request, Response> {

  protected final TransportService transportService;

  protected final ClusterService clusterService;

  protected final ThreadPool threadPool;

  protected TransportMasterNodeOperationAction(Settings settings, TransportService transportService,
      ClusterService clusterService, ThreadPool threadPool) {
    super(settings);
    this.transportService = transportService;
    this.clusterService = clusterService;
    this.threadPool = threadPool;

    transportService.registerHandler(transportAction(), new TransportHandler());
  }

  protected abstract String transportAction();

  protected abstract Request newRequest();

  protected abstract Response newResponse();

  protected abstract Response masterOperation(Request request) throws HermesException;

  @Override
  protected void doExecute(final Request request, final ActionListener<Response> listener) {
    DiscoveryNodes nodes = clusterService.state().nodes();
    if (nodes.localNodeMaster()) {
      threadPool.execute(new Runnable() {
        @Override
        public void run() {
          try {
            Response response = masterOperation(request);
            listener.onResponse(response);
          } catch (Exception e) {
            listener.onFailure(e);
          }
        }
      });
    } else {
      transportService.sendRequest(nodes.masterNode(), transportAction(), request,
          new BaseTransportResponseHandler<Response>() {
            @Override
            public Response newInstance() {
              return newResponse();
            }

            @Override
            public void handleResponse(Response response) {
              listener.onResponse(response);
            }

            @Override
            public void handleException(RemoteTransportException exp) {
              listener.onFailure(exp);
            }
          });
    }
  }

  private class TransportHandler extends BaseTransportRequestHandler<Request> {

    @Override
    public Request newInstance() {
      return newRequest();
    }

    @Override
    public void messageReceived(final Request request, final TransportChannel channel)
        throws Exception {
      if (clusterService.state().nodes().localNodeMaster()) {
        Response response = masterOperation(request);
        channel.sendResponse(response);
      } else {
        transportService.sendRequest(clusterService.state().nodes().masterNode(), transportAction(),
            request, new BaseTransportResponseHandler<Response>() {
              @Override
              public Response newInstance() {
                return newResponse();
              }

              @Override
              public void handleResponse(Response response) {
                try {
                  channel.sendResponse(response);
                } catch (Exception e) {
                  logger.error("Failed to send response", e);
                }
              }

              @Override
              public void handleException(RemoteTransportException exp) {
                try {
                  channel.sendResponse(exp);
                } catch (Exception e) {
                  logger.error("Failed to send response", e);
                }
              }
            });
      }
    }
  }
}
