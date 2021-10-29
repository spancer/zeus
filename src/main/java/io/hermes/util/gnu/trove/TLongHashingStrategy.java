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

import java.io.Serializable;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Interface to support pluggable hashing strategies in maps and sets. Implementors can use this
 * interface to make the trove hashing algorithms use an optimal strategy when computing hashcodes.
 * <p/>
 * Created: Sun Nov 4 08:56:06 2001
 *
 * @author Eric D. Friedman
 * @version $Id: PHashingStrategy.template,v 1.1 2006/11/10 23:28:00 robeden Exp $
 */

public interface TLongHashingStrategy extends Serializable {

  /**
   * Computes a hash code for the specified long. Implementors can use the long's own value or a
   * custom scheme designed to minimize collisions for a known set of input.
   *
   * @param val long for which the hashcode is to be computed
   * @return the hashCode
   */
  int computeHashCode(long val);
} // TLongHashingStrategy
