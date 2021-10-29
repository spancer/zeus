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


package io.hermes.action.support.lakehouse;

import io.hermes.action.ActionRequest;
import io.hermes.action.ActionRequestValidationException;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import java.io.IOException;

/**
 * @Author owencwl
 * @Description:
 * @Date 2021/5/19 13:19
 * @Version 1.0
 */
public class LakeHouseOperationRequest implements ActionRequest {

  private String operationString;

  public LakeHouseOperationRequest(String operationString) {
    this.operationString = operationString;
  }

  public LakeHouseOperationRequest() {
  }

  @Override
  public ActionRequestValidationException validate() {
    return null;
  }

  @Override
  public boolean listenerThreaded() {
    return false;
  }

  @Override
  public ActionRequest listenerThreaded(boolean listenerThreaded) {
    return null;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    operationString = in.readUTF();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeUTF(operationString);
  }
}
