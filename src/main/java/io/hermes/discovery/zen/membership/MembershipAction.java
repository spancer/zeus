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

package io.hermes.discovery.zen.membership;

import io.hermes.HermesException;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.transport.BaseTransportRequestHandler;
import io.hermes.transport.TransportChannel;
import io.hermes.transport.TransportService;
import io.hermes.transport.VoidTransportResponseHandler;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.io.stream.VoidStreamable;
import io.hermes.util.settings.Settings;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author spancer.ray
 */
public class MembershipAction extends AbstractComponent {

  private final TransportService transportService;
  private final MembershipListener listener;

  public MembershipAction(Settings settings, TransportService transportService,
      MembershipListener listener) {
    super(settings);
    this.transportService = transportService;
    this.listener = listener;

    transportService.registerHandler(JoinRequestRequestHandler.ACTION,
        new JoinRequestRequestHandler());
    transportService.registerHandler(LeaveRequestRequestHandler.ACTION,
        new LeaveRequestRequestHandler());
  }

  public void close() {
    transportService.removeHandler(JoinRequestRequestHandler.ACTION);
    transportService.removeHandler(LeaveRequestRequestHandler.ACTION);
  }

  public void sendLeaveRequest(DiscoveryNode masterNode, DiscoveryNode node) {
    transportService.sendRequest(node, LeaveRequestRequestHandler.ACTION,
        new LeaveRequest(masterNode), VoidTransportResponseHandler.INSTANCE_NOSPAWN);
  }

  public void sendLeaveRequestBlocking(DiscoveryNode masterNode, DiscoveryNode node,
      TimeValue timeout) throws HermesException, TimeoutException {
    transportService
        .submitRequest(masterNode, LeaveRequestRequestHandler.ACTION, new LeaveRequest(node),
            VoidTransportResponseHandler.INSTANCE_NOSPAWN)
        .txGet(timeout.millis(), TimeUnit.MILLISECONDS);
  }

  public void sendJoinRequest(DiscoveryNode masterNode, DiscoveryNode node) {
    transportService.sendRequest(masterNode, JoinRequestRequestHandler.ACTION,
        new JoinRequest(node), VoidTransportResponseHandler.INSTANCE_NOSPAWN);
  }

  public void sendJoinRequestBlocking(DiscoveryNode masterNode, DiscoveryNode node,
      TimeValue timeout) throws HermesException, TimeoutException {
    transportService
        .submitRequest(masterNode, JoinRequestRequestHandler.ACTION, new JoinRequest(node),
            VoidTransportResponseHandler.INSTANCE_NOSPAWN)
        .txGet(timeout.millis(), TimeUnit.MILLISECONDS);
  }

  public interface MembershipListener {

    void onJoin(DiscoveryNode node);

    void onLeave(DiscoveryNode node);
  }

  private static class JoinRequest implements Streamable {

    private DiscoveryNode node;

    private JoinRequest() {
    }

    private JoinRequest(DiscoveryNode node) {
      this.node = node;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      node = DiscoveryNode.readNode(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      node.writeTo(out);
    }
  }

  private static class LeaveRequest implements Streamable {

    private DiscoveryNode node;

    private LeaveRequest() {
    }

    private LeaveRequest(DiscoveryNode node) {
      this.node = node;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
      node = DiscoveryNode.readNode(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
      node.writeTo(out);
    }
  }

  private class JoinRequestRequestHandler extends BaseTransportRequestHandler<JoinRequest> {

    static final String ACTION = "discovery/zen/join";

    @Override
    public JoinRequest newInstance() {
      return new JoinRequest();
    }

    @Override
    public void messageReceived(JoinRequest request, TransportChannel channel) throws Exception {
      listener.onJoin(request.node);
      channel.sendResponse(VoidStreamable.INSTANCE);
    }
  }

  private class LeaveRequestRequestHandler extends BaseTransportRequestHandler<LeaveRequest> {

    static final String ACTION = "discovery/zen/leave";

    @Override
    public LeaveRequest newInstance() {
      return new LeaveRequest();
    }

    @Override
    public void messageReceived(LeaveRequest request, TransportChannel channel) throws Exception {
      listener.onLeave(request.node);
      channel.sendResponse(VoidStreamable.INSTANCE);
    }
  }
}
