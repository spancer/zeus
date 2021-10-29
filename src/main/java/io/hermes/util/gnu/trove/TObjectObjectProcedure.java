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

package io.hermes.util.gnu.trove;

/**
 * Interface for procedures that take two Object parameters.
 * <p/>
 * Created: Mon Nov 5 22:03:30 2001
 *
 * @author Eric D. Friedman
 * @version $Id: TObjectObjectProcedure.java,v 1.3 2006/11/10 23:27:57 robeden Exp $
 */

public interface TObjectObjectProcedure<K, V> {

  /**
   * Executes this procedure. A false return value indicates that the application executing this
   * procedure should not invoke this procedure again.
   *
   * @param a an <code>Object</code> value
   * @param b an <code>Object</code> value
   * @return true if additional invocations of the procedure are allowed.
   */
  boolean execute(K a, V b);
}// TObjectObjectProcedure
