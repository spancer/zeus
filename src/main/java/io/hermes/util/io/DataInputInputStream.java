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

package io.hermes.util.io;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper {@link java.io.InputStream} around {@link java.io.DataInput}.
 *
 * @author spancer.ray
 */
public class DataInputInputStream extends InputStream {

  private final DataInput dataInput;

  public DataInputInputStream(DataInput dataInput) {
    this.dataInput = dataInput;
  }

  @Override
  public int read() throws IOException {
    return dataInput.readByte();
  }

  @Override
  public long skip(long n) throws IOException {
    return dataInput.skipBytes((int) n);
  }
}
