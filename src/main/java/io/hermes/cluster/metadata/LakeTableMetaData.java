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


package io.hermes.cluster.metadata;

import static io.hermes.util.settings.ImmutableSettings.readSettingsFromStream;
import static io.hermes.util.settings.ImmutableSettings.settingsBuilder;
import static io.hermes.util.settings.ImmutableSettings.writeSettingsToStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.ImmutableMap;
import io.hermes.util.MapBuilder;
import io.hermes.util.Nullable;
import io.hermes.util.Preconditions;
import io.hermes.util.concurrent.Immutable;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.json.JsonBuilder;
import io.hermes.util.json.ToJson;
import io.hermes.util.settings.ImmutableSettings;
import io.hermes.util.settings.Settings;
import java.io.IOException;
import java.util.Map;

/**
 * Tables in LakeHouse which holds all tables of this tenant.
 *
 * @author spancer.ray
 */
@Immutable
public class LakeTableMetaData {


  public static final String SETTING_NUMBER_OF_REPLICAS = "laketable.number_of_replicas";

  private final String tableName;

  // private final LakeSpace lakeSpace;

  private final Settings settings;

  private final ImmutableMap<String, String> mappings;

  private LakeTableMetaData(String table, Settings settings,
      ImmutableMap<String, String> mappings) {
    Preconditions.checkArgument(settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, -1) != -1,
        "must specify numberOfReplicas for table [" + table + "]");
    this.tableName = table;
    this.settings = settings;
    this.mappings = mappings;
    // this.lakeSpace = LakeSpace.builder() //Needs to init here.
  }

  public static Builder newtableMetaDataBuilder(String table) {
    return new Builder(table);
  }

  public static Builder newtableMetaDataBuilder(LakeTableMetaData tableMetaData) {
    return new Builder(tableMetaData);
  }

  public String table() {
    return tableName;
  }

  public String getTable() {
    return table();
  }

  public int numberOfReplicas() {
    return settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 1);
  }

  public int getNumberOfReplicas() {
    return numberOfReplicas();
  }

  public Settings settings() {
    return settings;
  }

  public Settings getSettings() {
    return settings();
  }

  public ImmutableMap<String, String> mappings() {
    return mappings;
  }

  public ImmutableMap<String, String> getMappings() {
    return mappings();
  }

  public String mapping(String mappingType) {
    return mappings.get(mappingType);
  }

  public static class Builder {

    private String table;

    private Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;

    private MapBuilder<String, String> mappings = MapBuilder.newMapBuilder();

    public Builder(String table) {
      this.table = table;
    }

    public Builder(LakeTableMetaData tableMetaData) {
      this(tableMetaData.table());
      settings(tableMetaData.settings());
      mappings.putAll(tableMetaData.mappings);
    }

    public static void toJson(LakeTableMetaData tableMetaData, JsonBuilder builder,
        ToJson.Params params) throws IOException {
      builder.startObject(tableMetaData.table());

      builder.startObject("settings");
      for (Map.Entry<String, String> entry : tableMetaData.settings().getAsMap().entrySet()) {
        builder.field(entry.getKey(), entry.getValue());
      }
      builder.endObject();

      builder.startObject("mappings");
      for (Map.Entry<String, String> entry : tableMetaData.mappings().entrySet()) {
        builder.startObject(entry.getKey());
        builder.field("source", entry.getValue());
        builder.endObject();
      }
      builder.endObject();

      builder.endObject();
    }

    public static LakeTableMetaData fromJson(JsonParser jp, @Nullable Settings globalSettings)
        throws IOException {
      Builder builder = new Builder(jp.getCurrentName());

      String currentFieldName = null;
      JsonToken token = jp.nextToken();
      while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
        if (token == JsonToken.FIELD_NAME) {
          currentFieldName = jp.getCurrentName();
        } else if (token == JsonToken.START_OBJECT) {
          if ("settings".equals(currentFieldName)) {
            ImmutableSettings.Builder settingsBuilder =
                settingsBuilder().globalSettings(globalSettings);
            while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
              String key = jp.getCurrentName();
              token = jp.nextToken();
              String value = jp.getText();
              settingsBuilder.put(key, value);
            }
            builder.settings(settingsBuilder.build());
          } else if ("mappings".equals(currentFieldName)) {
            while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
              String mappingType = jp.getCurrentName();
              String mappingSource = null;
              while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
                if (token == JsonToken.FIELD_NAME) {
                  if ("source".equals(jp.getCurrentName())) {
                    jp.nextToken();
                    mappingSource = jp.getText();
                  }
                }
              }
              if (mappingSource == null) {
                // crap, no mapping source, warn?
              } else {
                builder.putMapping(mappingType, mappingSource);
              }
            }
          }
        }
      }
      return builder.build();
    }

    public static LakeTableMetaData readFrom(StreamInput in, Settings globalSettings)
        throws IOException {
      Builder builder = new Builder(in.readUTF());
      builder.settings(readSettingsFromStream(in, globalSettings));
      int mappingsSize = in.readVInt();
      for (int i = 0; i < mappingsSize; i++) {
        builder.putMapping(in.readUTF(), in.readUTF());
      }
      return builder.build();
    }

    public static void writeTo(LakeTableMetaData tableMetaData, StreamOutput out)
        throws IOException {
      out.writeUTF(tableMetaData.table());
      writeSettingsToStream(tableMetaData.settings(), out);
      out.writeVInt(tableMetaData.mappings().size());
      for (Map.Entry<String, String> entry : tableMetaData.mappings().entrySet()) {
        out.writeUTF(entry.getKey());
        out.writeUTF(entry.getValue());
      }
    }

    public String table() {
      return table;
    }

    public Builder numberOfReplicas(int numberOfReplicas) {
      settings =
          settingsBuilder().put(settings).put(SETTING_NUMBER_OF_REPLICAS, numberOfReplicas).build();
      return this;
    }

    public int numberOfReplicas() {
      return settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, -1);
    }

    public Builder settings(Settings.Builder settings) {
      this.settings = settings.build();
      return this;
    }

    public Builder settings(Settings settings) {
      this.settings = settings;
      return this;
    }

    public Builder removeMapping(String mappingType) {
      mappings.remove(mappingType);
      return this;
    }

    public Builder putMapping(String mappingType, String mappingSource) {
      mappings.put(mappingType, mappingSource);
      return this;
    }

    public LakeTableMetaData build() {
      return new LakeTableMetaData(table, settings, mappings.immutableMap());
    }
  }
}
