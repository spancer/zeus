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

package io.hermes.transport.local;

import static io.hermes.transport.Transport.Helper.isRequest;
import static io.hermes.transport.Transport.Helper.setRequest;
import static io.hermes.util.concurrent.ConcurrentMaps.newConcurrentMap;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.ActionNotFoundTransportException;
import io.hermes.transport.ConnectTransportException;
import io.hermes.transport.NodeNotConnectedException;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.ResponseHandlerFailureTransportException;
import io.hermes.transport.ResponseHandlerNotFoundTransportException;
import io.hermes.transport.Transport;
import io.hermes.transport.TransportException;
import io.hermes.transport.TransportRequestHandler;
import io.hermes.transport.TransportResponseHandler;
import io.hermes.transport.TransportSerializationException;
import io.hermes.transport.TransportServiceAdapter;
import io.hermes.util.Nullable;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.io.ThrowableObjectInputStream;
import io.hermes.util.io.stream.BytesStreamInput;
import io.hermes.util.io.stream.BytesStreamOutput;
import io.hermes.util.io.stream.HandlesStreamInput;
import io.hermes.util.io.stream.HandlesStreamOutput;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.settings.ImmutableSettings;
import io.hermes.util.settings.Settings;
import io.hermes.util.transport.BoundTransportAddress;
import io.hermes.util.transport.LocalTransportAddress;
import io.hermes.util.transport.TransportAddress;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author spancer.ray
 */
public class LocalTransport extends AbstractLifecycleComponent<Transport> implements Transport {

  private final static ConcurrentMap<TransportAddress, LocalTransport> transports =
      newConcurrentMap();
  private static final AtomicLong transportAddressIdGenerator = new AtomicLong();
  private final ThreadPool threadPool;
  private final ConcurrentMap<DiscoveryNode, LocalTransport> connectedNodes = newConcurrentMap();
  private volatile TransportServiceAdapter transportServiceAdapter;
  private volatile BoundTransportAddress boundAddress;
  private volatile LocalTransportAddress localAddress;

  public LocalTransport(ThreadPool threadPool) {
    this(ImmutableSettings.Builder.EMPTY_SETTINGS, threadPool);
  }

  @Inject
  public LocalTransport(Settings settings, ThreadPool threadPool) {
    super(settings);
    this.threadPool = threadPool;
  }

  @Override
  public TransportAddress addressFromString(String address) {
    return new LocalTransportAddress(address);
  }

  @Override
  public boolean addressSupported(Class<? extends TransportAddress> address) {
    return LocalTransportAddress.class.equals(address);
  }

  @Override
  protected void doStart() throws HermesException {
    localAddress =
        new LocalTransportAddress(Long.toString(transportAddressIdGenerator.incrementAndGet()));
    transports.put(localAddress, this);
    boundAddress = new BoundTransportAddress(localAddress, localAddress);
  }

  @Override
  protected void doStop() throws HermesException {
    transports.remove(localAddress);
    // now, go over all the transports connected to me, and raise disconnected event
    for (LocalTransport targetTransport : transports.values()) {
      for (Map.Entry<DiscoveryNode, LocalTransport> entry : targetTransport.connectedNodes
          .entrySet()) {
        if (entry.getValue() == this) {
          targetTransport.disconnectFromNode(entry.getKey());
        }
      }
    }
  }

  @Override
  protected void doClose() throws HermesException {
  }

  @Override
  public void transportServiceAdapter(TransportServiceAdapter transportServiceAdapter) {
    this.transportServiceAdapter = transportServiceAdapter;
  }

  @Override
  public BoundTransportAddress boundAddress() {
    return boundAddress;
  }

  @Override
  public boolean nodeConnected(DiscoveryNode node) {
    return connectedNodes.containsKey(node);
  }

  @Override
  public void connectToNode(DiscoveryNode node) throws ConnectTransportException {
    synchronized (this) {
      if (connectedNodes.containsKey(node)) {
        return;
      }
      final LocalTransport targetTransport = transports.get(node.address());
      if (targetTransport == null) {
        throw new ConnectTransportException(node, "Failed to connect");
      }
      connectedNodes.put(node, targetTransport);
      transportServiceAdapter.raiseNodeConnected(node);
    }
  }

  @Override
  public void disconnectFromNode(DiscoveryNode node) {
    synchronized (this) {
      LocalTransport removed = connectedNodes.remove(node);
      if (removed != null) {
        transportServiceAdapter.raiseNodeDisconnected(node);
      }
    }
  }

  @Override
  public <T extends Streamable> void sendRequest(final DiscoveryNode node, final long requestId,
      final String action, final Streamable message, final TransportResponseHandler<T> handler)
      throws IOException, TransportException {
    HandlesStreamOutput stream = BytesStreamOutput.Cached.cachedHandles();

    stream.writeLong(requestId);
    byte status = 0;
    status = setRequest(status);
    stream.writeByte(status); // 0 for request, 1 for response.

    stream.writeUTF(action);
    message.writeTo(stream);

    final LocalTransport targetTransport = connectedNodes.get(node);
    if (targetTransport == null) {
      throw new NodeNotConnectedException(node, "Node not connected");
    }

    final byte[] data = ((BytesStreamOutput) stream.wrappedOut()).copiedByteArray();
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        targetTransport.messageReceived(data, action, LocalTransport.this, handler);
      }
    });
  }

  ThreadPool threadPool() {
    return this.threadPool;
  }

  void messageReceived(byte[] data, String action, LocalTransport sourceTransport,
      @Nullable final TransportResponseHandler responseHandler) {
    StreamInput stream = new BytesStreamInput(data);
    stream = HandlesStreamInput.Cached.cached(stream);

    try {
      long requestId = stream.readLong();
      byte status = stream.readByte();
      boolean isRequest = isRequest(status);

      if (isRequest) {
        handleRequest(stream, requestId, sourceTransport);
      } else {
        final TransportResponseHandler handler = transportServiceAdapter.remove(requestId);
        if (handler == null) {
          throw new ResponseHandlerNotFoundTransportException(requestId);
        }
        if (Transport.Helper.isError(status)) {
          handlerResponseError(stream, handler);
        } else {
          handleResponse(stream, handler);
        }
      }
    } catch (Exception e) {
      if (responseHandler != null) {
        responseHandler
            .handleException(new RemoteTransportException(nodeName(), localAddress, action, e));
      } else {
        logger.warn("Failed to receive message for action [" + action + "]", e);
      }
    }
  }

  private void handleRequest(StreamInput stream, long requestId, LocalTransport sourceTransport)
      throws Exception {
    final String action = stream.readUTF();
    final LocalTransportChannel transportChannel =
        new LocalTransportChannel(this, sourceTransport, action, requestId);
    try {
      final TransportRequestHandler handler = transportServiceAdapter.handler(action);
      if (handler == null) {
        throw new ActionNotFoundTransportException("Action [" + action + "] not found");
      }
      final Streamable streamable = handler.newInstance();
      streamable.readFrom(stream);
      handler.messageReceived(streamable, transportChannel);
    } catch (Exception e) {
      try {
        transportChannel.sendResponse(e);
      } catch (IOException e1) {
        logger.warn("Failed to send error message back to client for action [" + action + "]", e);
        logger.warn("Actual Exception", e1);
      }
    }
  }


  private void handleResponse(StreamInput buffer, final TransportResponseHandler handler) {
    final Streamable streamable = handler.newInstance();
    try {
      streamable.readFrom(buffer);
    } catch (Exception e) {
      handleException(handler, new TransportSerializationException(
          "Failed to deserialize response of type [" + streamable.getClass().getName() + "]", e));
      return;
    }
    if (handler.spawn()) {
      threadPool.execute(new Runnable() {
        @SuppressWarnings({"unchecked"})
        @Override
        public void run() {
          try {
            handler.handleResponse(streamable);
          } catch (Exception e) {
            handleException(handler,
                new ResponseHandlerFailureTransportException("Failed to handler response", e));
          }
        }
      });
    } else {
      try {
        // noinspection unchecked
        handler.handleResponse(streamable);
      } catch (Exception e) {
        handleException(handler,
            new ResponseHandlerFailureTransportException("Failed to handler response", e));
      }
    }
  }

  private void handlerResponseError(StreamInput buffer, final TransportResponseHandler handler) {
    Throwable error;
    try {
      ThrowableObjectInputStream ois = new ThrowableObjectInputStream(buffer);
      error = (Throwable) ois.readObject();
    } catch (Exception e) {
      error = new TransportSerializationException(
          "Failed to deserialize exception response from stream", e);
    }
    handleException(handler, error);
  }

  private void handleException(final TransportResponseHandler handler, Throwable error) {
    if (!(error instanceof RemoteTransportException)) {
      error = new RemoteTransportException("None remote transport exception", error);
    }
    final RemoteTransportException rtx = (RemoteTransportException) error;
    handler.handleException(rtx);
  }
}
