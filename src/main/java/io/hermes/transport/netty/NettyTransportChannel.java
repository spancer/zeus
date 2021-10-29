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
/*
 * Copyright (c) Chalco-Steering Technology Co., Ltd. All Rights Reserved. This software is licensed
 * not sold. Use or reproduction of this software by any unauthorized individual or entity is
 * strictly prohibited. This software is the confidential and proprietary information of
 * Chalco-Steering Technology Co., Ltd. Disclosure of such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into with Chalco-Steering
 * Technology Co., Ltd. Chalco-Steering Technology Co., Ltd. MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * Chalco-Steering Technology Co., Ltd. SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ANY DERIVATIVES THEREOF.
 */
package io.hermes.transport.netty;

import static io.hermes.transport.Transport.Helper.setError;
import static io.hermes.transport.Transport.Helper.setResponse;
import java.io.IOException;
import java.io.NotSerializableException;
import io.hermes.transport.NotSerializableTransportException;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.TransportChannel;
import io.hermes.util.io.ThrowableObjectOutputStream;
import io.hermes.util.io.stream.BytesStreamOutput;
import io.hermes.util.io.stream.HandlesStreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * @author spancer.ray
 */
public class NettyTransportChannel implements TransportChannel {

  private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

  private final NettyTransport transport;

  private final String action;

  private final long requestId;

  private final Channel channel;

  public NettyTransportChannel(NettyTransport transport, String action, Channel channel,
      long requestId) {
    this.transport = transport;
    this.action = action;
    this.channel = channel;
    this.requestId = requestId;
  }

  @Override
  public String action() {
    return this.action;
  }

  @Override
  public Channel getChannel() {
    return channel;
  }

  @Override
  public void sendResponse(Streamable message) throws IOException {
    HandlesStreamOutput stream = BytesStreamOutput.Cached.cachedHandles();
    stream.writeBytes(LENGTH_PLACEHOLDER); // fake size
    stream.writeLong(requestId);
    byte status = 0;
    status = setResponse(status);
    stream.writeByte(status); // 0 for request, 1 for response.
    message.writeTo(stream);
    byte[] data = ((BytesStreamOutput) stream.wrappedOut()).copiedByteArray();
    ByteBuf buffer = Unpooled.wrappedBuffer(data);
    buffer.setInt(0, buffer.writerIndex() - 4); // update real size.
    ChannelFuture channelFuture = channel.writeAndFlush(buffer);
    channelFuture.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          System.out.println("回写response失败:" + future.cause());
        } else {
          System.out.println("回写response成功。");
        }
      }
    });
  }

  @Override
  public void sendResponse(Throwable error) throws IOException {
    BytesStreamOutput stream;
    try {
      stream = BytesStreamOutput.Cached.cached();
      writeResponseExceptionHeader(stream);
      RemoteTransportException tx = new RemoteTransportException(transport.nodeName(),
          transport.wrapAddress(channel.localAddress()), action, error);
      ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
      too.writeObject(tx);
      too.close();
    } catch (NotSerializableException e) {
      stream = BytesStreamOutput.Cached.cached();
      writeResponseExceptionHeader(stream);
      RemoteTransportException tx = new RemoteTransportException(transport.nodeName(),
          transport.wrapAddress(channel.localAddress()), action,
          new NotSerializableTransportException(error));
      ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
      too.writeObject(tx);
      too.close();
    }
    ByteBuf buffer = Unpooled.wrappedBuffer(stream.copiedByteArray());
    buffer.setInt(0, buffer.writerIndex() - 4); // update real size.
    channel.writeAndFlush(buffer);
  }

  private void writeResponseExceptionHeader(BytesStreamOutput stream) throws IOException {
    stream.writeBytes(LENGTH_PLACEHOLDER);
    stream.writeLong(requestId);
    byte status = 0;
    status = setResponse(status);
    status = setError(status);
    stream.writeByte(status);
  }
}
