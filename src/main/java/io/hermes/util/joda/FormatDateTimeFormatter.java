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

package io.hermes.util.joda;

import io.hermes.util.concurrent.Immutable;
import org.joda.time.format.DateTimeFormatter;

/**
 * A simple wrapper around {@link org.joda.time.format.DateTimeFormatter} that retains the format
 * that was used to create it.
 *
 * @author spancer.ray
 */
@Immutable
public class FormatDateTimeFormatter {

  private final String format;

  private final DateTimeFormatter parser;

  private final DateTimeFormatter printer;

  public FormatDateTimeFormatter(String format, DateTimeFormatter parser) {
    this(format, parser, parser);
  }

  public FormatDateTimeFormatter(String format, DateTimeFormatter parser,
      DateTimeFormatter printer) {
    this.format = format;
    this.parser = parser;
    this.printer = printer;
  }

  public String format() {
    return format;
  }

  public DateTimeFormatter parser() {
    return parser;
  }

  public DateTimeFormatter printer() {
    return this.printer;
  }
}
