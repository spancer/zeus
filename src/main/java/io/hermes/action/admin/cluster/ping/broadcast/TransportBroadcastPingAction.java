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

package io.hermes.action.admin.cluster.ping.broadcast;

import com.google.inject.Inject;
import io.hermes.action.ActionListener;
import io.hermes.action.TransportActions;
import io.hermes.action.support.broadcast.TransportBroadcastOperationAction;
import io.hermes.cluster.ClusterService;
import io.hermes.cluster.ClusterState;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.TransportService;
import io.hermes.util.settings.Settings;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author spancer.ray
 */
public class TransportBroadcastPingAction
    extends TransportBroadcastOperationAction<BroadcastPingRequest, BroadcastPingResponse> {

  @Inject
  public TransportBroadcastPingAction(Settings settings, ThreadPool threadPool,
      ClusterService clusterService, TransportService transportService) {
    super(settings, threadPool, clusterService, transportService);
  }

  @Override
  protected String transportAction() {
    return TransportActions.Admin.Cluster.Ping.BROADCAST;
  }

  @Override
  protected String transportShardAction() {
    return "/cluster/ping/broadcast/shard";
  }

  @Override
  protected BroadcastPingRequest newRequest() {
    return new BroadcastPingRequest();
  }


  @Override
  protected void doExecute(BroadcastPingRequest request,
      ActionListener<BroadcastPingResponse> listener) {
    super.doExecute(request, listener);
  }

  @Override
  protected BroadcastPingResponse newResponse(BroadcastPingRequest request,
      AtomicReferenceArray shardsResponses, ClusterState clusterState) {
    return new BroadcastPingResponse();
  }

}
