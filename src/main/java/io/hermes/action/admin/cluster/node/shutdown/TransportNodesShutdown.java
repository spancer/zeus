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

package io.hermes.action.admin.cluster.node.shutdown;

import static com.google.common.collect.Lists.newArrayList;
import static io.hermes.util.TimeValue.readTimeValue;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.HermesIllegalStateException;
import io.hermes.action.TransportActions;
import io.hermes.action.support.nodes.NodeOperationRequest;
import io.hermes.action.support.nodes.TransportNodesOperationAction;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.ClusterService;
import io.hermes.node.Node;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.TransportService;
import io.hermes.util.TimeValue;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.settings.Settings;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author spancer.ray
 */
public class TransportNodesShutdown extends
    TransportNodesOperationAction<NodesShutdownRequest, NodesShutdownResponse, TransportNodesShutdown.NodeShutdownRequest, NodesShutdownResponse.NodeShutdownResponse> {

  private final Node node;

  private final boolean disabled;

  @Inject
  public TransportNodesShutdown(Settings settings, ClusterName clusterName, ThreadPool threadPool,
      ClusterService clusterService, TransportService transportService,
      Node node) {
    super(settings, clusterName, threadPool, clusterService, transportService);
    this.node = node;
    disabled = componentSettings.getAsBoolean("disabled", false);
  }

  @Override
  protected String transportAction() {
    return TransportActions.Admin.Cluster.Node.SHUTDOWN;
  }

  @Override
  protected String transportNodeAction() {
    return "/cluster/nodes/shutdown/node";
  }

  @Override
  protected NodesShutdownResponse newResponse(NodesShutdownRequest nodesShutdownRequest,
      AtomicReferenceArray responses) {
    final List<NodesShutdownResponse.NodeShutdownResponse> nodeShutdownResponses = newArrayList();
    for (int i = 0; i < responses.length(); i++) {
      Object resp = responses.get(i);
      if (resp instanceof NodesShutdownResponse.NodeShutdownResponse) {
        nodeShutdownResponses.add((NodesShutdownResponse.NodeShutdownResponse) resp);
      }
    }
    return new NodesShutdownResponse(clusterName, nodeShutdownResponses
        .toArray(new NodesShutdownResponse.NodeShutdownResponse[nodeShutdownResponses.size()]));
  }

  @Override
  protected NodesShutdownRequest newRequest() {
    return new NodesShutdownRequest();
  }

  @Override
  protected NodeShutdownRequest newNodeRequest() {
    return new NodeShutdownRequest();
  }

  @Override
  protected NodeShutdownRequest newNodeRequest(String nodeId, NodesShutdownRequest request) {
    return new NodeShutdownRequest(nodeId, request.delay);
  }

  @Override
  protected NodesShutdownResponse.NodeShutdownResponse newNodeResponse() {
    return new NodesShutdownResponse.NodeShutdownResponse();
  }

  @Override
  protected NodesShutdownResponse.NodeShutdownResponse nodeOperation(NodeShutdownRequest request)
      throws HermesException {
    if (disabled) {
      throw new HermesIllegalStateException("Shutdown is disabled");
    }
    logger.info("Shutting down in [{}]", request.delay);
    threadPool.schedule(new Runnable() {
      @Override
      public void run() {
        node.close();
      }
    }, request.delay.millis(), TimeUnit.MILLISECONDS);
    return new NodesShutdownResponse.NodeShutdownResponse(
        clusterService.state().nodes().localNode());
  }

  @Override
  protected boolean accumulateExceptions() {
    return false;
  }

  protected static class NodeShutdownRequest extends NodeOperationRequest {

    TimeValue delay;

    private NodeShutdownRequest() {
    }

    private NodeShutdownRequest(String nodeId, TimeValue delay) {
      super(nodeId);
      this.delay = delay;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      super.readFrom(in);
      delay = readTimeValue(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      super.writeTo(out);
      delay.writeTo(out);
    }
  }
}
