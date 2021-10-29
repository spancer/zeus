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

package io.hermes.action.admin.cluster.state;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.action.TransportActions;
import io.hermes.action.support.master.TransportMasterNodeOperationAction;
import io.hermes.cluster.ClusterService;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.TransportService;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class TransportClusterStateAction extends
    TransportMasterNodeOperationAction<ClusterStateRequest, ClusterStateResponse> {

  @Inject
  public TransportClusterStateAction(Settings settings, TransportService transportService,
      ClusterService clusterService, ThreadPool threadPool) {
    super(settings, transportService, clusterService, threadPool);
  }

  @Override
  protected String transportAction() {
    return TransportActions.Admin.Cluster.STATE;
  }

  @Override
  protected ClusterStateRequest newRequest() {
    return new ClusterStateRequest();
  }

  @Override
  protected ClusterStateResponse newResponse() {
    return new ClusterStateResponse();
  }

  @Override
  protected ClusterStateResponse masterOperation(ClusterStateRequest request)
      throws HermesException {
    return new ClusterStateResponse(clusterService.state());
  }
}
