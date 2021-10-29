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

import io.hermes.util.concurrent.highscalelib.NonBlockingHashSet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author spancer.ray
 */
@Sharable
public class OpenChannelsHandler extends ChannelOutboundHandlerAdapter {

  private NonBlockingHashSet<Channel> openChannels = new NonBlockingHashSet<Channel>();

  private final ChannelFutureListener remover = new ChannelFutureListener() {
    public void operationComplete(ChannelFuture future) throws Exception {
      openChannels.remove(future.channel());
    }
  };


  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception {
    if (ctx.channel().isOpen()) {
      boolean added = openChannels.add(ctx.channel());
      if (added) {
        ctx.channel().closeFuture().addListener(remover);
      }
    }
    super.write(ctx, msg, promise);
  }

  public void close() {
    for (Channel channel : openChannels) {
      channel.close().awaitUninterruptibly();
    }
    openChannels.clear();
  }

}
