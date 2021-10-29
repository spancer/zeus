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

package io.hermes.transport.netty;

import static com.google.common.collect.Lists.newArrayList;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.HermesIllegalStateException;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.BindTransportException;
import io.hermes.transport.ConnectTransportException;
import io.hermes.transport.FailedCommunicationException;
import io.hermes.transport.NodeNotConnectedException;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.Transport;
import io.hermes.transport.TransportException;
import io.hermes.transport.TransportResponseHandler;
import io.hermes.transport.TransportServiceAdapter;
import io.hermes.util.SizeValue;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.concurrent.ConcurrentMaps;
import io.hermes.util.io.NetworkUtils;
import io.hermes.util.io.stream.BytesStreamOutput;
import io.hermes.util.io.stream.HandlesStreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.settings.ImmutableSettings.Builder;
import io.hermes.util.settings.Settings;
import io.hermes.util.transport.BoundTransportAddress;
import io.hermes.util.transport.InetSocketTransportAddress;
import io.hermes.util.transport.PortsRange;
import io.hermes.util.transport.TransportAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author spancer.ray
 */
public class NettyTransport extends AbstractLifecycleComponent<Transport> implements Transport {

  private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

  static {
    InternalLoggerFactory.setDefaultFactory(new NettyInternalHermesLoggerFactory() {
      @Override
      public InternalLogger newInstance(String name) {
        return super.newInstance(name.replace("io.netty.", "netty."));
      }
    });
  }

  final int workerCount;
  final String port;
  final String bindHost;
  final String publishHost;
  final TimeValue connectTimeout;
  final int connectionsPerNode;
  final Boolean tcpNoDelay;
  final Boolean tcpKeepAlive;
  final Boolean reuseAddress;
  final SizeValue tcpSendBufferSize;
  final SizeValue tcpReceiveBufferSize;
  // node id to actual channel
  final ConcurrentMap<String, NodeConnections> connectedNodes = ConcurrentMaps.newConcurrentMap();
  private final ThreadPool threadPool;
  private volatile OpenChannelsHandler serverOpenChannels;
  private volatile Bootstrap clientBootstrap;
  private volatile ServerBootstrap serverBootstrap;
  private volatile ChannelFuture serverChannel;
  private volatile TransportServiceAdapter transportServiceAdapter;
  private volatile BoundTransportAddress boundAddress;

  public NettyTransport(ThreadPool threadPool) {
    this(Builder.EMPTY_SETTINGS, threadPool);
  }

  @Inject
  public NettyTransport(Settings settings, ThreadPool threadPool) {
    super(settings);
    this.threadPool = threadPool;

    this.workerCount =
        componentSettings.getAsInt("worker_count", Runtime.getRuntime().availableProcessors());
    this.port = componentSettings.get("port", "9400-9500");
    this.bindHost = componentSettings.get("bind_host", "localhost");
    this.connectionsPerNode = componentSettings.getAsInt("connections_per_node", 5);
    this.publishHost = componentSettings.get("publish_host");
    this.connectTimeout =
        componentSettings.getAsTime("connect_timeout", TimeValue.timeValueSeconds(1L));
    this.tcpNoDelay = componentSettings.getAsBoolean("tcp_no_delay", true);
    this.tcpKeepAlive = componentSettings.getAsBoolean("tcp_keep_alive", null);
    this.reuseAddress =
        componentSettings.getAsBoolean("reuse_address", NetworkUtils.defaultReuseAddress());
    this.tcpSendBufferSize = componentSettings.getAsSize("tcp_send_buffer_size", null);
    this.tcpReceiveBufferSize = componentSettings.getAsSize("tcp_receive_buffer_size", null);
  }

  public Settings settings() {
    return this.settings;
  }

  @Override
  public void transportServiceAdapter(TransportServiceAdapter service) {
    this.transportServiceAdapter = service;
  }

  TransportServiceAdapter transportServiceAdapter() {
    return transportServiceAdapter;
  }

  ThreadPool threadPool() {
    return threadPool;
  }

  @Override
  protected void doStart() throws HermesException {
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    clientBootstrap = new Bootstrap();
    clientBootstrap.group(workerGroup);
    clientBootstrap.channel(NioSocketChannel.class);
    clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);

    clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
      @Override
      public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("decoder", new SizeHeaderFrameDecoder(256, true, true));
        ch.pipeline().addLast("dispatcher", new MessageChannelHandler(NettyTransport.this, logger));
      }
    });

    clientBootstrap.option(ChannelOption.SO_TIMEOUT,
        Long.valueOf(connectTimeout.millis()).intValue());
    if (tcpNoDelay != null) {
      clientBootstrap.option(ChannelOption.TCP_NODELAY, tcpNoDelay);
    }
    if (tcpKeepAlive != null) {
      clientBootstrap.option(ChannelOption.SO_KEEPALIVE, tcpKeepAlive);
    }
    if (tcpSendBufferSize != null) {
      clientBootstrap.option(ChannelOption.SO_SNDBUF,
          Long.valueOf(tcpSendBufferSize.bytes()).intValue());
    }
    if (tcpReceiveBufferSize != null) {
      clientBootstrap.option(ChannelOption.SO_RCVBUF,
          Long.valueOf(tcpReceiveBufferSize.bytes()).intValue());
    }
    if (reuseAddress != null) {
      clientBootstrap.option(ChannelOption.SO_REUSEADDR, reuseAddress);
    }

    if (!settings.getAsBoolean("network.server", true)) {
      return;
    }

    serverOpenChannels = new OpenChannelsHandler();

    // TODO needs to using threadpool here.
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup serverWorkerGroup = new NioEventLoopGroup();
    try {
      serverBootstrap = new ServerBootstrap();
      serverBootstrap.group(bossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast("openChannels", serverOpenChannels);
              ch.pipeline().addLast("decoder", new SizeHeaderFrameDecoder(256));// TODO
              ch.pipeline().addLast("dispatcher",
                  new MessageChannelHandler(NettyTransport.this, logger));
            }
          }).option(ChannelOption.SO_BACKLOG, 128);

      if (tcpNoDelay != null) {
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, tcpNoDelay);
      }
      if (tcpKeepAlive != null) {
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, tcpKeepAlive);
      }
      if (tcpSendBufferSize != null) {
        serverBootstrap.childOption(ChannelOption.SO_SNDBUF,
            Long.valueOf(tcpSendBufferSize.bytes()).intValue());
      }
      if (tcpReceiveBufferSize != null) {
        serverBootstrap.childOption(ChannelOption.SO_RCVBUF,
            Long.valueOf(tcpReceiveBufferSize.bytes()).intValue());
      }
      if (reuseAddress != null) {
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, reuseAddress);
        serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, reuseAddress);
      }

      // Bind and start to accept incoming connections.
      InetAddress hostAddressX;
      try {
        hostAddressX = NetworkUtils.resolveBindHostAddress(bindHost, settings);
      } catch (IOException e) {
        throw new BindTransportException("Failed to resolve host [" + bindHost + "]", e);
      }
      final InetAddress hostAddress = hostAddressX;

      PortsRange portsRange = new PortsRange(port);
      final AtomicReference<Exception> lastException = new AtomicReference<Exception>();
      boolean success = portsRange.iterate(new PortsRange.PortCallback() {
        @Override
        public boolean onPortNumber(int portNumber) {
          try {
            serverChannel =
                serverBootstrap.bind(new InetSocketAddress(hostAddress, portNumber)).sync();
          } catch (Exception e) {
            lastException.set(e);
            return false;
          }
          return true;
        }
      });
      if (!success) {
        throw new BindTransportException("Failed to bind to [" + port + "]", lastException.get());
      }

      logger.debug("Bound to address [{}]", serverChannel.channel().localAddress());

      InetSocketAddress boundAddress = (InetSocketAddress) serverChannel.channel().localAddress();
      InetSocketAddress publishAddress;
      try {
        publishAddress = new InetSocketAddress(boundAddress.getAddress(), boundAddress.getPort());
        this.boundAddress = new BoundTransportAddress(new InetSocketTransportAddress(boundAddress),
            new InetSocketTransportAddress(publishAddress));
      } catch (Exception e) {
        throw new BindTransportException("Failed to resolve publish address", e);
      } finally {

      }
    } catch (HermesException e1) {
      e1.printStackTrace();
    } finally {

    }

  }

  @Override
  protected void doStop() throws HermesException {
    if (serverChannel != null) {
      try {
        serverChannel.channel().close().awaitUninterruptibly();
      } finally {
        serverChannel = null;
      }
    }

    if (serverOpenChannels != null) {
      serverOpenChannels.close();
      serverOpenChannels = null;
    }

    if (serverBootstrap != null) {
      serverBootstrap = null;
    }

    for (Iterator<NodeConnections> it = connectedNodes.values().iterator(); it.hasNext(); ) {
      NodeConnections nodeConnections = it.next();
      it.remove();
      nodeConnections.close();
    }

    if (clientBootstrap != null) {
      // HACK, make sure we try and close open client channels also after
      // we releaseExternalResources, they seem to hang when there are open client
      // channels
      ScheduledFuture<?> scheduledFuture = threadPool.schedule(new Runnable() {
        @Override
        public void run() {
          try {
            for (Iterator<NodeConnections> it = connectedNodes.values().iterator();
                it.hasNext(); ) {
              NodeConnections nodeConnections = it.next();
              it.remove();
              nodeConnections.close();
            }
          } catch (Exception e) {
            // ignore
          }
        }
      }, 500, TimeUnit.MILLISECONDS);
      scheduledFuture.cancel(false);
      clientBootstrap = null;
    }
  }

  @Override
  protected void doClose() throws HermesException {
  }

  @Override
  public TransportAddress addressFromString(String address) throws Exception {
    int index = address.lastIndexOf(':');
    if (index == -1) {
      throw new HermesIllegalStateException(
          "Port must be provided to create inet address from [" + address + "]");
    }
    String host = address.substring(0, index);
    int port = Integer.parseInt(address.substring(index + 1));
    return new InetSocketTransportAddress(host, port);
  }

  @Override
  public boolean addressSupported(Class<? extends TransportAddress> address) {
    return InetSocketTransportAddress.class.equals(address);
  }

  @Override
  public BoundTransportAddress boundAddress() {
    return this.boundAddress;
  }

  TransportAddress wrapAddress(SocketAddress socketAddress) {
    return new InetSocketTransportAddress((InetSocketAddress) socketAddress);
  }

  @Override
  public <T extends Streamable> void sendRequest(DiscoveryNode node, long requestId, String action,
      Streamable streamable, final TransportResponseHandler<T> handler)
      throws IOException, TransportException {

    Channel targetChannel = nodeChannel(node);

    HandlesStreamOutput stream = BytesStreamOutput.Cached.cachedHandles();
    stream.writeBytes(LENGTH_PLACEHOLDER); // fake size

    stream.writeLong(requestId);
    byte status = 0;
    status = Helper.setRequest(status);
    stream.writeByte(status); // 0 for request, 1 for response.

    stream.writeUTF(action);
    streamable.writeTo(stream);

    byte[] data = ((BytesStreamOutput) stream.wrappedOut()).copiedByteArray();
    ByteBuf buffer = Unpooled.wrappedBuffer(data);

    int size = buffer.writerIndex() - 4;
    if (size == 0) {
      handler.handleException(new RemoteTransportException("",
          new FailedCommunicationException("Trying to send a stream with 0 size")));
    }
    buffer.setInt(0, size); // update real size.
    ChannelFuture channelFuture = targetChannel.writeAndFlush(buffer);
//     TODO do we need this listener?
    channelFuture.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          // maybe add back the retry?
          handler.handleException(new RemoteTransportException("", new
              FailedCommunicationException("Error sending request", future.cause())));
        }
      }
    });
  }

  @Override
  public boolean nodeConnected(DiscoveryNode node) {
    return connectedNodes.containsKey(node.id());
  }

  @Override
  public void connectToNode(DiscoveryNode node) {
    if (!lifecycle.started()) {
      throw new HermesIllegalStateException("Can't add nodes to a stopped transport");
    }
    try {
      if (node == null) {
        throw new ConnectTransportException(node, "Can't connect to a null node");
      }
      NodeConnections nodeConnections = connectedNodes.get(node.id());
      if (nodeConnections != null) {
        return;
      }
      synchronized (this) {
        // recheck here, within the sync block (we cache connections, so we don't care
        // about this single sync block)
        nodeConnections = connectedNodes.get(node.id());
        if (nodeConnections != null) {
          return;
        }
        List<ChannelFuture> connectFutures = newArrayList();
        for (int connectionIndex = 0; connectionIndex < connectionsPerNode; connectionIndex++) {
          InetSocketAddress address = ((InetSocketTransportAddress) node.address()).address();
          connectFutures.add(clientBootstrap.connect(address));

        }
        List<Channel> channels = newArrayList();
        Throwable lastConnectException = null;
        for (ChannelFuture connectFuture : connectFutures) {
          if (!lifecycle.started()) {
            for (Channel channel : channels) {
              channel.close().awaitUninterruptibly();
            }
            throw new ConnectTransportException(node,
                "Can't connect when the transport is stopped");
          }
          connectFuture.awaitUninterruptibly((long) (connectTimeout.millis() * 1.25));
          if (!connectFuture.isSuccess()) {
            lastConnectException = connectFuture.cause();
          } else {
            Channel channel = connectFuture.channel();
            channel.closeFuture().addListener(new ChannelCloseListener(node.id()));
            channels.add(channel);
          }
        }
        if (channels.isEmpty()) {
          if (lastConnectException != null) {
            throw new ConnectTransportException(node, "connect_timeout[" + connectTimeout + "]",
                lastConnectException);
          }
          throw new ConnectTransportException(node,
              "connect_timeout[" + connectTimeout + "], reason unknown");
        }
        if (logger.isDebugEnabled()) {
          logger.debug("Connected to node[{}], number_of_connections[{}]", node, channels.size());
        }
        connectedNodes.put(node.id(),
            new NodeConnections(node, channels.toArray(new Channel[channels.size()])));
        transportServiceAdapter.raiseNodeConnected(node);
      }
    } catch (Exception e) {
      throw new ConnectTransportException(node, "General node connection failure", e);
    }
  }

  @Override
  public void disconnectFromNode(DiscoveryNode node) {
    NodeConnections nodeConnections = connectedNodes.remove(node.id());
    if (nodeConnections != null) {
      nodeConnections.close();
    }
  }

  private Channel nodeChannel(DiscoveryNode node) throws ConnectTransportException {
    NettyTransport.NodeConnections nodeConnections = connectedNodes.get(node.id());
    if (nodeConnections == null) {
      throw new NodeNotConnectedException(node, "Node not connected");
    }
    Channel channel = nodeConnections.channel();
    if (channel == null) {
      throw new NodeNotConnectedException(node, "Node not connected");
    }
    return channel;
  }

  public class NodeConnections {

    private final DiscoveryNode node;

    private final AtomicInteger counter = new AtomicInteger();

    private volatile Channel[] channels;

    private volatile boolean closed = false;

    private NodeConnections(DiscoveryNode node, Channel[] channels) {
      this.node = node;
      this.channels = channels;
    }

    private Channel channel() {
      return channels[Math.abs(counter.incrementAndGet()) % channels.length];
    }

    private void channelClosed(Channel closedChannel) {
      List<Channel> updated = newArrayList();
      for (Channel channel : channels) {
        if (!channel.id().equals(closedChannel.id())) {
          updated.add(channel);
        }
      }
      this.channels = updated.toArray(new Channel[updated.size()]);
    }

    private int numberOfChannels() {
      return channels.length;
    }

    private synchronized void close() {
      if (closed) {
        return;
      }
      closed = true;
      Channel[] channelsToClose = channels;
      channels = new Channel[0];
      for (Channel channel : channelsToClose) {
        if (channel.isOpen()) {
          channel.close().awaitUninterruptibly();
        }
      }
      logger.debug("Disconnected from [{}]", node);
      transportServiceAdapter.raiseNodeDisconnected(node);
    }
  }

  private class ChannelCloseListener implements ChannelFutureListener {

    private final String nodeId;

    private ChannelCloseListener(String nodeId) {
      this.nodeId = nodeId;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
      final NodeConnections nodeConnections = connectedNodes.get(nodeId);
      if (nodeConnections != null) {
        nodeConnections.channelClosed(future.channel());
        if (nodeConnections.numberOfChannels() == 0) {
          // all the channels in the node connections are closed, remove it from
          // our client channels
          connectedNodes.remove(nodeId);
          // and close it
          nodeConnections.close();
        }
      }
    }
  }
}
