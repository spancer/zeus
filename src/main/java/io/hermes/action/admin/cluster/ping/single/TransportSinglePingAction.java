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

package io.hermes.action.admin.cluster.ping.single;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.action.TransportActions;
import io.hermes.action.support.single.TransportSingleOperationAction;
import io.hermes.cluster.ClusterService;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.TransportService;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class TransportSinglePingAction extends
    TransportSingleOperationAction<SinglePingRequest, SinglePingResponse> {

  @Inject
  public TransportSinglePingAction(Settings settings, ThreadPool threadPool,
      ClusterService clusterService, TransportService transportService) {
    super(settings, threadPool, clusterService, transportService);
  }

  @Override
  protected String transportAction() {
    return TransportActions.Admin.Cluster.Ping.SINGLE;
  }

  @Override
  protected String transportShardAction() {
    return "/cluster/ping/single/shard";
  }

  @Override
  protected SinglePingResponse shardOperation(SinglePingRequest request, int shardId)
      throws HermesException {
    return new SinglePingResponse();
  }

  @Override
  protected SinglePingRequest newRequest() {
    return new SinglePingRequest();
  }

  @Override
  protected SinglePingResponse newResponse() {
    return new SinglePingResponse();
  }
}
