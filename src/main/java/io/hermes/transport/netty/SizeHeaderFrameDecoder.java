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

import io.hermes.util.logging.HermesLogger;
import io.hermes.util.logging.Loggers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import java.io.StreamCorruptedException;

/**
 * @author spancer.ray
 */
public class SizeHeaderFrameDecoder extends LineBasedFrameDecoder {

  private final HermesLogger logger = Loggers.getLogger(SizeHeaderFrameDecoder.class);

  public SizeHeaderFrameDecoder(int maxLength, boolean stripDelimiter, boolean failFast) {
    super(maxLength, stripDelimiter, failFast);
  }

  public SizeHeaderFrameDecoder(int maxLength) {
    super(maxLength, true, false);
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
    if (buffer.readableBytes() < 4) {
      return null;
    }
    int dataLen = buffer.getInt(buffer.readerIndex());
    if (dataLen <= 0) {
      throw new StreamCorruptedException("invalid data length: " + dataLen);
    }
    if (buffer.readableBytes() < dataLen + 4) {
      return null;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("starting decode for byte buffer.");
      logger.debug("byteBuf to string: {}", convertByteBufToString(buffer));
      logger.debug("buffer readable bytes: {}", buffer.readableBytes());
      logger.debug("buffer data length: {}", dataLen);
    }
    buffer.skipBytes(4);
    logger.info("skip 4 Bytes after: {}", buffer);
    return buffer;
  }

  public String convertByteBufToString(ByteBuf buf) {
    String str;
    if (buf.hasArray()) { // 处理堆缓冲区
      str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
    } else { // 处理直接缓冲区以及复合缓冲区
      byte[] bytes = new byte[buf.readableBytes()];
      buf.getBytes(buf.readerIndex(), bytes);
      str = new String(bytes, 0, buf.readableBytes());
    }
    return str;
  }
}
