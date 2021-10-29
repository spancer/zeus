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

import io.hermes.util.io.stream.StreamInput;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

/**
 * @author spancer.ray
 */
public class ByteBufferStreamInput extends StreamInput {

  private final ByteBuf buffer;

  public ByteBufferStreamInput(ByteBuf buffer) {
    this.buffer = buffer;
  }

  @Override
  public byte readByte() throws IOException {
    return buffer.readByte();
  }

  @Override
  public void readBytes(byte[] b, int offset, int len) throws IOException {
    buffer.readBytes(b, offset, len);
  }

  @Override
  public void reset() throws IOException {
    buffer.resetReaderIndex();
  }

  @Override
  public void close() throws IOException {
    // nothing to do here
  }
}
