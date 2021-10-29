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

package io.hermes.transport;

import static io.hermes.util.concurrent.ConcurrentMaps.newConcurrentMap;
import static io.hermes.util.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.threadpool.ThreadPool;
import io.hermes.timer.TimerService;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.concurrent.highscalelib.NonBlockingHashMapLong;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.settings.Settings;
import io.hermes.util.timer.Timeout;
import io.hermes.util.timer.TimerTask;
import io.hermes.util.transport.BoundTransportAddress;
import io.hermes.util.transport.TransportAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author spancer.ray
 */
public class TransportService extends AbstractLifecycleComponent<TransportService> {


  final ConcurrentMap<String, TransportRequestHandler> serverHandlers = newConcurrentMap();
  final NonBlockingHashMapLong<RequestHolder> clientHandlers =
      new NonBlockingHashMapLong<RequestHolder>();
  final AtomicLong requestIds = new AtomicLong();
  final CopyOnWriteArrayList<TransportConnectionListener> connectionListeners =
      new CopyOnWriteArrayList<TransportConnectionListener>();
  private final Transport transport;
  private final ThreadPool threadPool;
  private final TimerService timerService;
  private boolean throwConnectException = false;

  public TransportService(Transport transport, ThreadPool threadPool, TimerService timerService) {
    this(EMPTY_SETTINGS, transport, threadPool, timerService);
  }

  @Inject
  public TransportService(Settings settings, Transport transport, ThreadPool threadPool,
      TimerService timerService) {
    super(settings);
    this.transport = transport;
    this.threadPool = threadPool;
    this.timerService = timerService;
  }

  @Override
  protected void doStart() throws HermesException {
    // register us as an adapter for the transport service
    transport.transportServiceAdapter(new Adapter());
    transport.start();
    if (transport.boundAddress() != null && logger.isInfoEnabled()) {
      logger.info("{}", transport.boundAddress());
    }
  }

  @Override
  protected void doStop() throws HermesException {
    transport.stop();
  }

  @Override
  protected void doClose() throws HermesException {
    transport.close();
  }

  public boolean addressSupported(Class<? extends TransportAddress> address) {
    return transport.addressSupported(address);
  }

  public BoundTransportAddress boundAddress() {
    return transport.boundAddress();
  }

  public boolean nodeConnected(DiscoveryNode node) {
    return transport.nodeConnected(node);
  }

  public void connectToNode(DiscoveryNode node) throws ConnectTransportException {
    transport.connectToNode(node);
  }

  public void disconnectFromNode(DiscoveryNode node) {
    transport.disconnectFromNode(node);
  }

  public void addConnectionListener(TransportConnectionListener listener) {
    connectionListeners.add(listener);
  }

  public void removeConnectionListener(TransportConnectionListener listener) {
    connectionListeners.remove(listener);
  }

  /**
   * Set to <tt>true</tt> to indicate that a {@link ConnectTransportException} should be thrown when
   * sending a message (otherwise, it will be passed to the response handler). Defaults to
   * <tt>false</tt>.
   *
   * <p>
   * This is useful when logic based on connect failure is needed without having to wrap the
   * handler, for example, in case of retries across several nodes.
   */
  public void throwConnectException(boolean throwConnectException) {
    this.throwConnectException = throwConnectException;
  }

  public <T extends Streamable> TransportFuture<T> submitRequest(DiscoveryNode node, String action,
      Streamable message, TransportResponseHandler<T> handler) throws TransportException {
    return submitRequest(node, action, message, null, handler);
  }

  public <T extends Streamable> TransportFuture<T> submitRequest(DiscoveryNode node, String action,
      Streamable message, TimeValue timeout, TransportResponseHandler<T> handler)
      throws TransportException {
    PlainTransportFuture<T> futureHandler = new PlainTransportFuture<T>(handler);
    sendRequest(node, action, message, timeout, futureHandler);
    return futureHandler;
  }

  public <T extends Streamable> void sendRequest(final DiscoveryNode node, final String action,
      final Streamable message, final TransportResponseHandler<T> handler)
      throws TransportException {
    sendRequest(node, action, message, null, handler);
  }

  public <T extends Streamable> void sendRequest(final DiscoveryNode node, final String action,
      final Streamable message, final TimeValue timeout, final TransportResponseHandler<T> handler)
      throws TransportException {
    final long requestId = newRequestId();
    try {
      Timeout timeoutX = null;
      if (timeout != null) {
        timeoutX = timerService.newTimeout(new TimeoutTimerTask(requestId), timeout);
      }
      clientHandlers.put(requestId, new RequestHolder<T>(handler, node, action, timeoutX));
      transport.sendRequest(node, requestId, action, message, handler);
    } catch (final Exception e) {
      // usually happen either because we failed to connect to the node
      // or because we failed serializing the message
      clientHandlers.remove(requestId);
      if (throwConnectException) {
        if (e instanceof ConnectTransportException) {
          throw (ConnectTransportException) e;
        }
      }
      // callback that an exception happened, but on a different thread since we don't
      // want handlers to worry about stack overflows
      threadPool.execute(new Runnable() {
        @Override
        public void run() {
          handler.handleException(new SendRequestTransportException(node, action, e));
        }
      });
    }
  }

  private long newRequestId() {
    return requestIds.getAndIncrement();
  }

  public TransportAddress addressFromString(String address) throws Exception {
    return transport.addressFromString(address);
  }

  public void registerHandler(ActionTransportRequestHandler handler) {
    registerHandler(handler.action(), handler);
  }

  public void registerHandler(String action, TransportRequestHandler handler) {
    TransportRequestHandler handlerReplaced = serverHandlers.put(action, handler);
    if (handlerReplaced != null) {
      logger.warn("Registered two transport handlers for action {}, handlers: {}, {}", action,
          handler, handlerReplaced);
    }
  }

  public void removeHandler(String action) {
    serverHandlers.remove(action);
  }

  static class RequestHolder<T extends Streamable> {

    private final TransportResponseHandler<T> handler;

    private final DiscoveryNode node;

    private final String action;

    private final Timeout timeout;

    RequestHolder(TransportResponseHandler<T> handler, DiscoveryNode node, String action,
        Timeout timeout) {
      this.handler = handler;
      this.node = node;
      this.action = action;
      this.timeout = timeout;
    }

    public TransportResponseHandler<T> handler() {
      return handler;
    }

    public DiscoveryNode node() {
      return this.node;
    }

    public String action() {
      return this.action;
    }

    public Timeout timeout() {
      return timeout;
    }
  }

  class Adapter implements TransportServiceAdapter {

    @Override
    public TransportRequestHandler handler(String action) {
      TransportRequestHandler transportRequestHandler = serverHandlers.get(action);
      logger.debug("current_action: {}, serverHandlers_size: {}, serverHandlers_values: {}", action,
          serverHandlers.keySet().size(), serverHandlers.keySet().toString());
      return transportRequestHandler;
    }

    @Override
    public TransportResponseHandler remove(long requestId) {
      RequestHolder holder = clientHandlers.remove(requestId);
      if (holder == null) {
        return null;
      }
      if (holder.timeout() != null) {
        holder.timeout().cancel();
      }
      return holder.handler();
    }

    @Override
    public void raiseNodeConnected(DiscoveryNode node) {
      for (TransportConnectionListener connectionListener : connectionListeners) {
        connectionListener.onNodeConnected(node);
      }
    }

    @Override
    public void raiseNodeDisconnected(DiscoveryNode node) {
      for (TransportConnectionListener connectionListener : connectionListeners) {
        connectionListener.onNodeDisconnected(node);
      }
      // node got disconnected, raise disconnection on possible ongoing handlers
      for (Map.Entry<Long, RequestHolder> entry : clientHandlers.entrySet()) {
        RequestHolder holder = entry.getValue();
        if (holder.node().equals(node)) {
          holder = clientHandlers.remove(entry.getKey());
          if (holder != null) {
            holder.handler()
                .handleException(new NodeDisconnectedTransportException(node, holder.action()));
          }
        }
      }
    }
  }

  class TimeoutTimerTask implements TimerTask {

    private final long requestId;

    TimeoutTimerTask(long requestId) {
      this.requestId = requestId;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
      if (timeout.isCancelled()) {
        return;
      }
      RequestHolder holder = clientHandlers.remove(requestId);
      if (holder != null) {
        holder.handler()
            .handleException(new ReceiveTimeoutTransportException(holder.node(), holder.action()));
      }
    }
  }
}
