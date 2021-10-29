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



package io.hermes.util.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.hermes.HermesException;
import io.hermes.util.ThreadLocals;
import io.hermes.util.io.FastByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author spancer.ray
 */
public class BinaryJsonBuilder extends JsonBuilder<BinaryJsonBuilder> {

  private final FastByteArrayOutputStream bos;
  private final JsonFactory factory;

  public BinaryJsonBuilder() throws IOException {
    this(Jackson.defaultJsonFactory());
  }

  public BinaryJsonBuilder(JsonFactory factory) throws IOException {
    this.bos = new FastByteArrayOutputStream();
    this.factory = factory;
    this.generator = factory.createJsonGenerator(bos, JsonEncoding.UTF8);
    this.builder = this;
  }

  public BinaryJsonBuilder(JsonGenerator generator) throws IOException {
    this.bos = null;
    this.generator = generator;
    this.factory = null;
    this.builder = this;
  }

  @Override
  public BinaryJsonBuilder raw(byte[] json) throws IOException {
    flush();
    bos.write(json);
    return this;
  }

  @Override
  public BinaryJsonBuilder reset() throws IOException {
    fieldCaseConversion = globalFieldCaseConversion;
    bos.reset();
    generator = factory.createJsonGenerator(bos, JsonEncoding.UTF8);
    return this;
  }

  public FastByteArrayOutputStream unsafeStream() throws IOException {
    flush();
    return bos;
  }

  @Override
  public byte[] unsafeBytes() throws IOException {
    flush();
    return bos.unsafeByteArray();
  }

  @Override
  public int unsafeBytesLength() throws IOException {
    flush();
    return bos.size();
  }

  @Override
  public byte[] copiedBytes() throws IOException {
    flush();
    return bos.copiedByteArray();
  }

  @Override
  public String string() throws IOException {
    flush();
    return new String(bos.unsafeByteArray(), Charset.defaultCharset());
  }

  /**
   * A thread local based cache of {@link BinaryJsonBuilder}.
   */
  public static class Cached {

    private static final ThreadLocal<ThreadLocals.CleanableValue<BinaryJsonBuilder>> cache =
        new ThreadLocal<ThreadLocals.CleanableValue<BinaryJsonBuilder>>() {
          @Override
          protected ThreadLocals.CleanableValue<BinaryJsonBuilder> initialValue() {
            try {
              BinaryJsonBuilder builder = new BinaryJsonBuilder();
              builder.cachedStringBuilder = new StringBuilder();
              return new ThreadLocals.CleanableValue<BinaryJsonBuilder>(builder);
            } catch (IOException e) {
              throw new HermesException("Failed to create json generator", e);
            }
          }
        };

    /**
     * Returns the cached thread local generator, with its internal {@link StringBuilder} cleared.
     */
    static BinaryJsonBuilder cached() throws IOException {
      ThreadLocals.CleanableValue<BinaryJsonBuilder> cached = cache.get();
      cached.get().reset();
      return cached.get();
    }
  }
}
