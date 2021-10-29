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

import io.hermes.transport.NotSerializableTransportException;
import io.hermes.transport.RemoteTransportException;
import io.hermes.transport.Transport;
import io.hermes.transport.TransportChannel;
import io.hermes.util.io.ThrowableObjectOutputStream;
import io.hermes.util.io.stream.BytesStreamOutput;
import io.hermes.util.io.stream.HandlesStreamOutput;
import io.hermes.util.io.stream.Streamable;
import io.netty.channel.Channel;
import java.io.IOException;
import java.io.NotSerializableException;

/**
 * @author spancer.ray
 */
public class LocalTransportChannel implements TransportChannel {

  private final LocalTransport sourceTransport;

  // the transport we will *send to*
  private final LocalTransport targetTransport;

  private final String action;

  private final long requestId;

  public LocalTransportChannel(LocalTransport sourceTransport, LocalTransport targetTransport,
      String action, long requestId) {
    this.sourceTransport = sourceTransport;
    this.targetTransport = targetTransport;
    this.action = action;
    this.requestId = requestId;
  }

  @Override
  public Channel getChannel() {
    return null;
  }

  @Override
  public String action() {
    return action;
  }

  @Override
  public void sendResponse(Streamable message) throws IOException {
    HandlesStreamOutput stream = BytesStreamOutput.Cached.cachedHandles();
    stream.writeLong(requestId);
    byte status = 0;
    status = Transport.Helper.setResponse(status);
    stream.writeByte(status); // 0 for request, 1 for response.
    message.writeTo(stream);
    final byte[] data = ((BytesStreamOutput) stream.wrappedOut()).copiedByteArray();
    targetTransport.threadPool().execute(new Runnable() {
      @Override
      public void run() {
        targetTransport.messageReceived(data, action, sourceTransport, null);
      }
    });
  }

  @Override
  public void sendResponse(Throwable error) throws IOException {
    BytesStreamOutput stream;
    try {
      stream = BytesStreamOutput.Cached.cached();
      writeResponseExceptionHeader(stream);
      RemoteTransportException tx = new RemoteTransportException(targetTransport.nodeName(),
          targetTransport.boundAddress().boundAddress(), action, error);
      ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
      too.writeObject(tx);
      too.close();
    } catch (NotSerializableException e) {
      stream = BytesStreamOutput.Cached.cached();
      writeResponseExceptionHeader(stream);
      RemoteTransportException tx = new RemoteTransportException(targetTransport.nodeName(),
          targetTransport.boundAddress().boundAddress(), action,
          new NotSerializableTransportException(error));
      ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
      too.writeObject(tx);
      too.close();
    }
    final byte[] data = stream.copiedByteArray();
    targetTransport.threadPool().execute(new Runnable() {
      @Override
      public void run() {
        targetTransport.messageReceived(data, action, sourceTransport, null);
      }
    });
  }

  private void writeResponseExceptionHeader(BytesStreamOutput stream) throws IOException {
    stream.writeLong(requestId);
    byte status = 0;
    status = Transport.Helper.setResponse(status);
    status = Transport.Helper.setError(status);
    stream.writeByte(status);
  }
}
