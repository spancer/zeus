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

package io.hermes.util;

/**
 * @author spancer.ray
 */
public class OsUtils {

  /**
   * The value of <tt>System.getProperty("os.name")<tt>.
   */
  public static final String OS_NAME = System.getProperty("os.name");
  /**
   * True iff running on Linux.
   */
  public static final boolean LINUX = OS_NAME.startsWith("Linux");
  /**
   * True iff running on Windows.
   */
  public static final boolean WINDOWS = OS_NAME.startsWith("Windows");
  /**
   * True iff running on SunOS.
   */
  public static final boolean SUN_OS = OS_NAME.startsWith("SunOS");


  private OsUtils() {

  }
}
