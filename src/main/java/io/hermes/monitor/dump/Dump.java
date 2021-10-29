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

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/**
 * @author spancer.ray
 */
public interface Dump {

  long timestamp();

  Map<String, Object> context();

  String cause();

  File createFile(String name) throws DumpException;

  Writer createFileWriter(String name) throws DumpException;

  OutputStream createFileOutputStream(String name) throws DumpException;

  File[] files();

  void finish() throws DumpException;
}
