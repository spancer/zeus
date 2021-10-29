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

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Interface for procedures with one short parameter.
 * <p/>
 * Created: Mon Nov 5 21:45:49 2001
 *
 * @author Eric D. Friedman
 * @version $Id: PProcedure.template,v 1.2 2007/11/01 16:08:14 robeden Exp $
 */

public interface TShortProcedure {

  /**
   * Executes this procedure. A false return value indicates that the application executing this
   * procedure should not invoke this procedure again.
   *
   * @param value a value of type <code>short</code>
   * @return true if additional invocations of the procedure are allowed.
   */
  boolean execute(short value);
}// TShortProcedure
