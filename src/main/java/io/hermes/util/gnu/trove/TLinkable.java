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


/**
 * Interface for Objects which can be inserted into a TLinkedList.
 * <p/>
 * <p>
 * Created: Sat Nov 10 15:23:41 2001
 * </p>
 *
 * @author Eric D. Friedman
 * @version $Id: TLinkable.java,v 1.2 2001/12/03 00:16:25 ericdf Exp $
 * @see io.hermes.util.gnu.trove.TLinkedList
 */

public interface TLinkable extends Serializable {

  /**
   * Returns the linked list node after this one.
   *
   * @return a <code>TLinkable</code> value
   */
  TLinkable getNext();

  /**
   * Sets the linked list node after this one.
   *
   * @param linkable a <code>TLinkable</code> value
   */
  void setNext(TLinkable linkable);

  /**
   * Returns the linked list node before this one.
   *
   * @return a <code>TLinkable</code> value
   */
  TLinkable getPrevious();

  /**
   * Sets the linked list node before this one.
   *
   * @param linkable a <code>TLinkable</code> value
   */
  void setPrevious(TLinkable linkable);
}// TLinkable
