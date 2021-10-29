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

import io.hermes.util.io.stream.VoidStreamable;

/**
 * @author spancer.ray
 */
public class VoidTransportResponseHandler implements TransportResponseHandler<VoidStreamable> {

  public static final VoidTransportResponseHandler INSTANCE =
      new VoidTransportResponseHandler(true);
  public static final VoidTransportResponseHandler INSTANCE_NOSPAWN =
      new VoidTransportResponseHandler(false);

  private boolean spawn;

  public VoidTransportResponseHandler() {
    this(true);
  }

  public VoidTransportResponseHandler(boolean spawn) {
    this.spawn = spawn;
  }

  @Override
  public VoidStreamable newInstance() {
    return VoidStreamable.INSTANCE;
  }

  @Override
  public void handleResponse(VoidStreamable response) {
  }

  @Override
  public void handleException(RemoteTransportException exp) {
  }

  @Override
  public boolean spawn() {
    return spawn;
  }
}
