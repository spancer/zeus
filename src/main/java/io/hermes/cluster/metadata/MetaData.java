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

import static com.google.common.collect.Sets.newHashSet;
import static io.hermes.util.MapBuilder.newMapBuilder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import io.hermes.util.MapBuilder;
import io.hermes.util.Nullable;
import io.hermes.util.concurrent.Immutable;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.json.BinaryJsonBuilder;
import io.hermes.util.json.JsonBuilder;
import io.hermes.util.json.ToJson;
import io.hermes.util.settings.Settings;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author spancer.ray
 */
@Immutable
public class MetaData implements Iterable<LakeTableMetaData> {

  public static MetaData EMPTY_META_DATA = newMetaDataBuilder().build();

  private final ImmutableMap<String, LakeTableMetaData> tables;

  // LakeTable's partitions settings.
  // TODO needs to ADD private final ImmutableMap<String, Integer> fieldsPartitions;

  private final String[] allTables;

  // // TODO needs to ADD private final ImmutableMap<String, String[]> spaceTables;
  // // TODO needs to ADD private final ImmutableMap<String, ImmutableSet<String>> spaceTableSet;

  private MetaData(ImmutableMap<String, LakeTableMetaData> tables) {
    this.tables = ImmutableMap.copyOf(tables);
    // build all tables map
    List<String> allTablesList = Lists.newArrayList();
    for (LakeTableMetaData tableMetaData : tables.values()) {
      allTablesList.add(tableMetaData.table());
    }
    allTables = allTablesList.toArray(new String[allTablesList.size()]);

    // build aliasAndtable to table map
    MapBuilder<String, Set<String>> tmpAliasAndtableTotableBuilder = newMapBuilder();
    for (LakeTableMetaData tableMetaData : tables.values()) {
      Set<String> lst = tmpAliasAndtableTotableBuilder.get(tableMetaData.table());
      if (lst == null) {
        lst = newHashSet();
        tmpAliasAndtableTotableBuilder.put(tableMetaData.table(), lst);
      }
      lst.add(tableMetaData.table());

    }
  }

  public static Builder newMetaDataBuilder() {
    return new Builder();
  }

  public String[] getAllTables() {
    return allTables;
  }

  public boolean hastable(String table) {
    return tables.containsKey(table);
  }

  public LakeTableMetaData table(String table) {
    return tables.get(table);
  }

  public ImmutableMap<String, LakeTableMetaData> indices() {
    return this.tables;
  }

  public ImmutableMap<String, LakeTableMetaData> getIndices() {
    return indices();
  }

  @Override
  public UnmodifiableIterator<LakeTableMetaData> iterator() {
    return tables.values().iterator();
  }

  public static class Builder {

    private MapBuilder<String, LakeTableMetaData> tables = newMapBuilder();

    public static String toJson(MetaData metaData) throws IOException {
      BinaryJsonBuilder builder = JsonBuilder.jsonBuilder().prettyPrint();
      builder.startObject();
      toJson(metaData, builder, ToJson.EMPTY_PARAMS);
      builder.endObject();
      return builder.string();
    }

    public static void toJson(MetaData metaData, JsonBuilder builder, ToJson.Params params)
        throws IOException {
      builder.startObject("meta-data");
      builder.startObject("tables");
      for (LakeTableMetaData tableMetaData : metaData) {
        LakeTableMetaData.Builder.toJson(tableMetaData, builder, params);
      }
      builder.endObject();
      builder.endObject();
    }

    public static MetaData fromJson(JsonParser jp, @Nullable Settings globalSettings)
        throws IOException {
      Builder builder = new Builder();
      String currentFieldName = null;
      JsonToken token = jp.nextToken();
      while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
        if (token == JsonToken.FIELD_NAME) {
          currentFieldName = jp.getCurrentName();
        } else if (token == JsonToken.START_OBJECT) {
          if ("tables".equals(currentFieldName)) {
            while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
              builder.put(LakeTableMetaData.Builder.fromJson(jp, globalSettings));
            }
          }
        }
      }
      return builder.build();
    }

    public static MetaData readFrom(StreamInput in, @Nullable Settings globalSettings)
        throws IOException {
      Builder builder = new Builder();
      int size = in.readVInt();
      for (int i = 0; i < size; i++) {
        builder.put(LakeTableMetaData.Builder.readFrom(in, globalSettings));
      }
      return builder.build();
    }

    public static void writeTo(MetaData metaData, StreamOutput out) throws IOException {
      out.writeVInt(metaData.tables.size());
      for (LakeTableMetaData tableMetaData : metaData) {
        LakeTableMetaData.Builder.writeTo(tableMetaData, out);
      }
    }

    public Builder put(LakeTableMetaData.Builder tableMetaDataBuilder) {
      return put(tableMetaDataBuilder.build());
    }

    public Builder put(LakeTableMetaData tableMetaData) {
      tables.put(tableMetaData.table(), tableMetaData);
      return this;
    }

    public LakeTableMetaData get(String table) {
      return tables.get(table);
    }

    public Builder remove(String table) {
      tables.remove(table);
      return this;
    }

    public Builder metaData(MetaData metaData) {
      tables.putAll(metaData.tables);
      return this;
    }

    public MetaData build() {
      return new MetaData(tables.immutableMap());
    }
  }
}
