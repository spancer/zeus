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


package io.hermes.action.support.single;

import io.hermes.HermesException;
import io.hermes.action.ActionListener;
import io.hermes.action.ActionResponse;
import io.hermes.action.support.BaseAction;
import io.hermes.cluster.ClusterService;
import io.hermes.cluster.ClusterState;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.BaseTransportRequestHandler;
import io.hermes.transport.TransportChannel;
import io.hermes.transport.TransportService;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.settings.Settings;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public abstract class TransportSingleOperationAction<Request extends SingleOperationRequest, Response extends ActionResponse> extends
    BaseAction<Request, Response> {

  protected final ClusterService clusterService;

  protected final TransportService transportService;

//  protected final IndicesService indicesService;

  protected final ThreadPool threadPool;

  protected TransportSingleOperationAction(Settings settings, ThreadPool threadPool,
      ClusterService clusterService, TransportService transportService) {
    super(settings);
    this.clusterService = clusterService;
    this.transportService = transportService;
    this.threadPool = threadPool;

    transportService.registerHandler(transportAction(), new TransportHandler());
    transportService.registerHandler(transportShardAction(), new ShardTransportHandler());
  }

  @Override
  protected void doExecute(Request request, ActionListener<Response> listener) {
    new AsyncSingleAction(request, listener).start();
  }

  protected abstract String transportAction();

  protected abstract String transportShardAction();

  protected abstract Response shardOperation(Request request, int shardId) throws HermesException;

  protected abstract Request newRequest();

  protected abstract Response newResponse();

  private class AsyncSingleAction {

    private final ActionListener<Response> listener;

    private final Request request;
    private final DiscoveryNodes nodes;

    private AsyncSingleAction(Request request, ActionListener<Response> listener) {
      this.request = request;
      this.listener = listener;

      ClusterState clusterState = clusterService.state();

      nodes = clusterState.nodes();

    }

    public void start() {
    }

    public void onFailure(Exception e) {
    }


  }

  private class TransportHandler extends BaseTransportRequestHandler<Request> {

    @Override
    public Request newInstance() {
      return newRequest();
    }

    @Override
    public void messageReceived(Request request, final TransportChannel channel) throws Exception {
      // no need to have a threaded listener since we just send back a response
      request.listenerThreaded(false);
      // if we have a local operation, execute it on a thread since we don't spawn
      request.threadedOperation(true);
      execute(request, new ActionListener<Response>() {
        @Override
        public void onResponse(Response result) {
          try {
            channel.sendResponse(result);
          } catch (Exception e) {
            onFailure(e);
          }
        }

        @Override
        public void onFailure(Throwable e) {
          try {
            channel.sendResponse(e);
          } catch (Exception e1) {
            logger.warn("Failed to send response for get", e1);
          }
        }
      });
    }

    @Override
    public boolean spawn() {
      return false;
    }
  }

  private class ShardTransportHandler extends
      BaseTransportRequestHandler<ShardSingleOperationRequest> {

    @Override
    public ShardSingleOperationRequest newInstance() {
      return new ShardSingleOperationRequest();
    }

    @Override
    public void messageReceived(ShardSingleOperationRequest request, TransportChannel channel)
        throws Exception {
      Response response = shardOperation(request.request(), request.shardId());
      channel.sendResponse(response);
    }
  }

  protected class ShardSingleOperationRequest implements Streamable {

    private Request request;

    private int shardId;

    ShardSingleOperationRequest() {
    }

    public ShardSingleOperationRequest(Request request, int shardId) {
      this.request = request;
      this.shardId = shardId;
    }

    public Request request() {
      return request;
    }

    public int shardId() {
      return shardId;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      request = newRequest();
      request.readFrom(in);
      shardId = in.readInt();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      request.writeTo(out);
      out.writeInt(shardId);
    }
  }
}
