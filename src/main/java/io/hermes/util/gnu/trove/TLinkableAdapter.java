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
 * Adapter for TLinkable interface which implements the interface and can therefore be extended
 * trivially to create TLinkable objects without having to implement the obvious.
 * <p/>
 * <p>
 * Created: Thurs Nov 15 16:25:00 2001
 * </p>
 *
 * @author Jason Baldridge
 * @version $Id: TLinkableAdapter.java,v 1.1 2006/11/10 23:27:56 robeden Exp $
 * @see io.hermes.util.gnu.trove.TLinkedList
 */

public class TLinkableAdapter implements TLinkable {

  TLinkable _previous, _next;

  /**
   * Returns the linked list node after this one.
   *
   * @return a <code>TLinkable</code> value
   */
  public TLinkable getNext() {
    return _next;
  }

  /**
   * Sets the linked list node after this one.
   *
   * @param linkable a <code>TLinkable</code> value
   */
  public void setNext(TLinkable linkable) {
    _next = linkable;
  }

  /**
   * Returns the linked list node before this one.
   *
   * @return a <code>TLinkable</code> value
   */
  public TLinkable getPrevious() {
    return _previous;
  }

  /**
   * Sets the linked list node before this one.
   *
   * @param linkable a <code>TLinkable</code> value
   */
  public void setPrevious(TLinkable linkable) {
    _previous = linkable;
  }
}
