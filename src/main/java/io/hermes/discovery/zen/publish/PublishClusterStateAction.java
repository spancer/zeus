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

package io.hermes.discovery.zen.publish;

import io.hermes.cluster.ClusterState;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.discovery.zen.DiscoveryNodesProvider;
import io.hermes.transport.BaseTransportRequestHandler;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.TransportChannel;
import io.hermes.transport.TransportService;
import io.hermes.transport.VoidTransportResponseHandler;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.io.stream.VoidStreamable;
import io.hermes.util.settings.Settings;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class PublishClusterStateAction extends AbstractComponent {

  private final TransportService transportService;
  private final DiscoveryNodesProvider nodesProvider;
  private final NewClusterStateListener listener;

  public PublishClusterStateAction(Settings settings, TransportService transportService,
      DiscoveryNodesProvider nodesProvider, NewClusterStateListener listener) {
    super(settings);
    this.transportService = transportService;
    this.nodesProvider = nodesProvider;
    this.listener = listener;
    transportService.registerHandler(PublishClusterStateRequestHandler.ACTION,
        new PublishClusterStateRequestHandler());
  }

  public void close() {
    transportService.removeHandler(PublishClusterStateRequestHandler.ACTION);
  }

  public void publish(ClusterState clusterState) {
    DiscoveryNode localNode = nodesProvider.nodes().localNode();
    for (final DiscoveryNode node : clusterState.nodes()) {
      if (node.equals(localNode)) {
        // no need to send to our self
        continue;
      }
      transportService.sendRequest(node, PublishClusterStateRequestHandler.ACTION,
          new PublishClusterStateRequest(clusterState), new VoidTransportResponseHandler(false) {
            @Override
            public void handleException(RemoteTransportException exp) {
              logger.warn(
                  "Failed to send cluster state to [{}], should be detected as failed soon...", exp,
                  node);
            }
          });
    }
  }

  public interface NewClusterStateListener {

    void onNewClusterState(ClusterState clusterState);
  }

  private class PublishClusterStateRequest implements Streamable {

    private ClusterState clusterState;

    private PublishClusterStateRequest() {
    }

    private PublishClusterStateRequest(ClusterState clusterState) {
      this.clusterState = clusterState;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      clusterState = ClusterState.Builder.readFrom(in, settings, nodesProvider.nodes().localNode());
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      ClusterState.Builder.writeTo(clusterState, out);
    }
  }

  private class PublishClusterStateRequestHandler
      extends BaseTransportRequestHandler<PublishClusterStateRequest> {

    static final String ACTION = "discovery/zen/publish";

    @Override
    public PublishClusterStateRequest newInstance() {
      return new PublishClusterStateRequest();
    }

    @Override
    public void messageReceived(PublishClusterStateRequest request, TransportChannel channel)
        throws Exception {
      listener.onNewClusterState(request.clusterState);
      channel.sendResponse(VoidStreamable.INSTANCE);
    }
  }
}
