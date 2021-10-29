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

package io.hermes.monitor.dump;

import com.google.common.collect.ImmutableMap;
import io.hermes.util.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author spancer.ray
 */
public abstract class AbstractDump implements Dump {

  private final long timestamp;

  private final String cause;

  private final Map<String, Object> context;

  private final ArrayList<File> files = new ArrayList<File>();

  protected AbstractDump(long timestamp, String cause, @Nullable Map<String, Object> context) {
    this.timestamp = timestamp;
    this.cause = cause;
    if (context == null) {
      context = ImmutableMap.of();
    }
    this.context = context;
  }

  @Override
  public long timestamp() {
    return timestamp;
  }

  @Override
  public Map<String, Object> context() {
    return this.context;
  }

  @Override
  public String cause() {
    return cause;
  }

  @Override
  public File[] files() {
    return files.toArray(new File[files.size()]);
  }

  @Override
  public File createFile(String name) throws DumpException {
    File file = doCreateFile(name);
    files.add(file);
    return file;
  }

  protected abstract File doCreateFile(String name) throws DumpException;

  @Override
  public OutputStream createFileOutputStream(String name) throws DumpException {
    try {
      return new FileOutputStream(createFile(name));
    } catch (FileNotFoundException e) {
      throw new DumpException("Failed to create file [" + name + "]", e);
    }
  }

  @Override
  public Writer createFileWriter(String name) throws DumpException {
    try {
      return new FileWriter(createFile(name));
    } catch (IOException e) {
      throw new DumpException("Failed to create file [" + name + "]", e);
    }
  }
}
