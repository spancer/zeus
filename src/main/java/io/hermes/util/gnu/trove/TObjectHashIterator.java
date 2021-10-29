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
 * Created: Wed Nov 28 21:30:53 2001
 *
 * @author Eric D. Friedman
 * @version $Id: TObjectHashIterator.java,v 1.2 2006/11/10 23:27:56 robeden Exp $
 */

class TObjectHashIterator<E> extends THashIterator<E> {

  protected final TObjectHash<E> _objectHash;

  public TObjectHashIterator(TObjectHash<E> hash) {
    super(hash);
    _objectHash = hash;
  }

  protected E objectAtIndex(int index) {
    return (E) _objectHash._set[index];
  }

} // TObjectHashIterator
