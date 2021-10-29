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


package io.hermes.lakehouse.engine;


import io.hermes.lakehouse.LakeHouseComponent;
import io.hermes.util.Nullable;
import io.hermes.util.component.CloseableComponent;
import io.hermes.util.concurrent.ThreadSafe;
import javax.management.Query;

/**
 * @author spancer.ray
 */
@ThreadSafe
public interface Engine extends LakeHouseComponent, CloseableComponent {

  /**
   * Starts the Engine.
   *
   * <p>
   * Note, after the creation and before the call to start, the store might be changed.
   */
  void start() throws EngineException;

  void create(Create create) throws EngineException;

  void delete(Delete delete) throws EngineException;


  class Refresh {

    private final boolean waitForOperations;

    public Refresh(boolean waitForOperations) {
      this.waitForOperations = waitForOperations;
    }

    public boolean waitForOperations() {
      return waitForOperations;
    }

    @Override
    public String toString() {
      return "waitForOperations[" + waitForOperations + "]";
    }
  }

  class Flush {

    private boolean refresh = false;

    /**
     * Should a refresh be performed after flushing. Defaults to <tt>false</tt>.
     */
    public boolean refresh() {
      return this.refresh;
    }

    /**
     * Should a refresh be performed after flushing. Defaults to <tt>false</tt>.
     */
    public Flush refresh(boolean refresh) {
      this.refresh = refresh;
      return this;
    }

    @Override
    public String toString() {
      return "refresh[" + refresh + "]";
    }
  }

  class Optimize {

    private boolean waitForMerge = true;
    private int maxNumSegments = -1;
    private boolean onlyExpungeDeletes = false;
    private boolean flush = false;
    private boolean refresh = false;

    public Optimize() {
    }

    public boolean waitForMerge() {
      return waitForMerge;
    }

    public Optimize waitForMerge(boolean waitForMerge) {
      this.waitForMerge = waitForMerge;
      return this;
    }

    public int maxNumSegments() {
      return maxNumSegments;
    }

    public Optimize maxNumSegments(int maxNumSegments) {
      this.maxNumSegments = maxNumSegments;
      return this;
    }

    public boolean onlyExpungeDeletes() {
      return onlyExpungeDeletes;
    }

    public Optimize onlyExpungeDeletes(boolean onlyExpungeDeletes) {
      this.onlyExpungeDeletes = onlyExpungeDeletes;
      return this;
    }

    public boolean flush() {
      return flush;
    }

    public Optimize flush(boolean flush) {
      this.flush = flush;
      return this;
    }

    public boolean refresh() {
      return refresh;
    }

    public Optimize refresh(boolean refresh) {
      this.refresh = refresh;
      return this;
    }

    @Override
    public String toString() {
      return "waitForMerge[" + waitForMerge + "], maxNumSegments[" + maxNumSegments
          + "], onlyExpungeDeletes[" + onlyExpungeDeletes + "], flush[" + flush + "], refresh["
          + refresh + "]";
    }
  }

  /**
   * lake house crate
   *
   * @author spancer
   */
  class LakeHouseCreate {

    private final String type;
    private final String id;
    private final byte[] source;

    public LakeHouseCreate(String type, String id,
        byte[] source) {
      this.type = type;
      this.id = id;
      this.source = source;
    }


    public String id() {
      return this.id;
    }

    public String type() {
      return this.type;
    }

    public byte[] source() {
      return this.source;
    }
  }


  class Create {

    private final String type;
    private final String id;
    private final byte[] source;

    public Create(String type, String id, byte[] source) {
      this.type = type;
      this.id = id;
      this.source = source;
    }

    public String type() {
      return this.type;
    }

    public String id() {
      return this.id;
    }

    public byte[] source() {
      return this.source;
    }
  }

  class Delete {


  }

  class DeleteByQuery {

    private final Query query;
    private final String queryParserName;
    private final byte[] source;
    private final String[] types;

    public DeleteByQuery(Query query, byte[] source, @Nullable String queryParserName,
        String... types) {
      this.query = query;
      this.source = source;
      this.queryParserName = queryParserName;
      this.types = types;
    }

    public String queryParserName() {
      return this.queryParserName;
    }

    public Query query() {
      return this.query;
    }

    public byte[] source() {
      return this.source;
    }

    public String[] types() {
      return this.types;
    }
  }
}
