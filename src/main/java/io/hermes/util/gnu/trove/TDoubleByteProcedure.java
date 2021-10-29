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
 * Interface for procedures that take two parameters of type double and byte.
 * <p/>
 * Created: Mon Nov 5 22:03:30 2001
 *
 * @author Eric D. Friedman
 * @version $Id: P2PProcedure.template,v 1.1 2006/11/10 23:28:00 robeden Exp $
 */

public interface TDoubleByteProcedure {

  /**
   * Executes this procedure. A false return value indicates that the application executing this
   * procedure should not invoke this procedure again.
   *
   * @param a a <code>double</code> value
   * @param b a <code>byte</code> value
   * @return true if additional invocations of the procedure are allowed.
   */
  boolean execute(double a, byte b);
}// TDoubleByteProcedure
