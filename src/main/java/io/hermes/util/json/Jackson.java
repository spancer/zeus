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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.hermes.util.joda.FormatDateTimeFormatter;
import io.hermes.util.joda.Joda;
import java.io.IOException;
import java.util.Date;

/**
 * A set of helper methods for Jackson.
 *
 * @author spancer.ray
 */
public final class Jackson {

  private static final JsonFactory defaultJsonFactory;

  static {
    defaultJsonFactory = newJsonFactory();
  }

  private Jackson() {

  }

  public static JsonFactory defaultJsonFactory() {
    return defaultJsonFactory;
  }

  public static JsonFactory newJsonFactory() {
    JsonFactory jsonFactory = new JsonFactory();
    jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    jsonFactory.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
    return jsonFactory;
  }

  public static class DateDeserializer extends StdDeserializer<Date> {

    private final FormatDateTimeFormatter formatter = Joda.forPattern("dateTime");

    public DateDeserializer() {
      super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.VALUE_STRING) {
        return new Date(formatter.parser().parseMillis(jp.getText()));
      }
      throw ctxt.mappingException(getValueClass());
    }
  }

}
