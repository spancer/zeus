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

import static io.hermes.transport.Transport.Helper.isError;
import static io.hermes.transport.Transport.Helper.isRequest;

import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.ActionNotFoundTransportException;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.ResponseHandlerFailureTransportException;
import io.hermes.transport.ResponseHandlerNotFoundTransportException;
import io.hermes.transport.TransportRequestHandler;
import io.hermes.transport.TransportResponseHandler;
import io.hermes.transport.TransportSerializationException;
import io.hermes.transport.TransportServiceAdapter;
import io.hermes.util.io.ThrowableObjectInputStream;
import io.hermes.util.io.stream.HandlesStreamInput;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.Streamable;
import io.hermes.util.logging.HermesLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class MessageChannelHandler extends ChannelInboundHandlerAdapter {

  private final HermesLogger logger;

  private final ThreadPool threadPool;

  private final TransportServiceAdapter transportServiceAdapter;

  private final NettyTransport transport;

  public MessageChannelHandler(NettyTransport transport, HermesLogger logger) {
    this.threadPool = transport.threadPool();
    this.transportServiceAdapter = transport.transportServiceAdapter();
    this.transport = transport;
    this.logger = logger;
  }

  /**
   * 通道被激活时进行处理，可以打印连接情况
   *
   * @param ctx
   * @throws Exception
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    logger.info("有新客户端接入: {}", ctx.channel().localAddress());
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    logger.info("netty通道注册: {}", ctx.channel().isRegistered());
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    logger.info("取消netty通道注册: {}", ctx.channel().isRegistered());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    logger.info("通道失去连接：{}", ctx.channel().isActive());
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    logger.debug("userEventTriggered");
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf buffer = (ByteBuf) msg;
    StreamInput streamIn = new ByteBufferStreamInput(buffer);
    streamIn = HandlesStreamInput.Cached.cached(streamIn);
    long requestId = buffer.readLong();
    byte status = buffer.readByte();
    boolean isRequest = isRequest(status);
    if (isRequest) {
      handleRequest(msg, streamIn, ctx.channel(), requestId);
    } else {
      final TransportResponseHandler handler = transportServiceAdapter.remove(requestId);
      if (handler == null) {
        throw new ResponseHandlerNotFoundTransportException(requestId);
      }
      if (isError(status)) {
        handlerResponseError(streamIn, handler);
      } else {
        handleResponse(streamIn, handler);
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
    if (handler.spawn()) {
      threadPool.execute(new Runnable() {
        @Override
        public void run() {
          try {
            handler.handleException(rtx);
          } catch (Exception e) {
            logger.error("Failed to handle exception response", e);
          }
        }
      });
    } else {
      handler.handleException(rtx);
    }
  }

  private void handleRequest(Object event, StreamInput buffer, Channel channel, long requestId)
      throws IOException {
    final String action = buffer.readUTF();
    ByteBuf bf = (ByteBuf) event;
    logger.info("current request action for server: {}", action);

    final NettyTransportChannel transportChannel =
        new NettyTransportChannel(transport, action, channel, requestId);
    try {
      final TransportRequestHandler handler = transportServiceAdapter.handler(action);
      if (handler == null) {
        throw new ActionNotFoundTransportException("Action [" + action + "] not found");
      }
      final Streamable streamable = handler.newInstance();
      streamable.readFrom(buffer);
      if (handler.spawn()) {
        threadPool.execute(new Runnable() {
          @SuppressWarnings({"unchecked"})
          @Override
          public void run() {
            try {
              handler.messageReceived(streamable, transportChannel);
            } catch (Throwable e) {
              try {
                transportChannel.sendResponse(e);
              } catch (IOException e1) {
                logger.warn(
                    "Failed to send error message back to client for action [" + action + "]", e1);
                logger.warn("Actual Exception", e);
              }
            }
          }
        });
      } else {
        // noinspection unchecked
        handler.messageReceived(streamable, transportChannel);
      }
    } catch (Exception e) {
      try {
        transportChannel.sendResponse(e);
      } catch (IOException e1) {
        logger.warn("Failed to send error message back to client for action [" + action + "]", e);
        logger.warn("Actual Exception", e1);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
  }
}
