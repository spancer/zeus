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

package io.hermes.action.admin.cluster.health;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.action.TransportActions;
import io.hermes.action.support.master.TransportMasterNodeOperationAction;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.ClusterService;
import io.hermes.threadpool.ThreadPool;
import io.hermes.timer.TimerService;
import io.hermes.transport.TransportService;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class TransportClusterHealthAction
    extends TransportMasterNodeOperationAction<ClusterHealthRequest, ClusterHealthResponse> {

  private final ClusterName clusterName;

  private final TimerService timerService;

  @Inject
  public TransportClusterHealthAction(Settings settings, TransportService transportService,
      ClusterService clusterService, ThreadPool threadPool, TimerService timerService,
      ClusterName clusterName) {
    super(settings, transportService, clusterService, threadPool);
    this.clusterName = clusterName;
    this.timerService = timerService;
  }

  @Override
  protected String transportAction() {
    return TransportActions.Admin.Cluster.HEALTH;
  }

  @Override
  protected ClusterHealthRequest newRequest() {
    return new ClusterHealthRequest();
  }

  @Override
  protected ClusterHealthResponse newResponse() {
    return new ClusterHealthResponse();
  }

  @Override
  protected ClusterHealthResponse masterOperation(ClusterHealthRequest request)
      throws HermesException {
    int waitFor = 2;
    if (request.waitForStatus() == null) {
      waitFor--;
    }
    if (request.waitForRelocatingShards() == -1) {
      waitFor--;
    }
    if (waitFor == 0) {
      // no need to wait for anything
      return clusterHealth(request);
    }
    long endTime = System.currentTimeMillis() + request.timeout().millis();
    while (true) {
      int waitForCounter = 0;
      ClusterHealthResponse response = clusterHealth(request);
      if (request.waitForStatus() != null
          && response.status().value() <= request.waitForStatus().value()) {
        waitForCounter++;
      }
      if (request.waitForRelocatingShards() != -1
          && response.relocatingShards() <= request.waitForRelocatingShards()) {
        waitForCounter++;
      }
      if (waitForCounter == waitFor) {
        return response;
      }
      if (timerService.estimatedTimeInMillis() > endTime) {
        response.timedOut = true;
        return response;
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        response.timedOut = true;
        // we got interrupted, bail
        return response;
      }
    }
  }

  private ClusterHealthResponse clusterHealth(ClusterHealthRequest request) {

    return new ClusterHealthResponse();
  }
}
