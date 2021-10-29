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

package io.hermes.util.concurrent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Immutable
 * <p/>
 * The class to which this annotation is applied is immutable. This means that its state cannot be
 * seen to change by callers. Of necessity this means that all public fields are final, and that all
 * public final reference fields refer to other immutable objects, and that methods do not publish
 * references to any internal state which is mutable by implementation even if not by design.
 * Immutable objects may still have internal mutable state for purposes of performance optimization;
 * some state variables may be lazily computed, so long as they are computed from immutable state
 * and that callers cannot tell the difference.
 * <p/>
 * Immutable objects are inherently thread-safe; they may be passed between threads or published
 * without synchronization.
 *
 * @author spancer.ray
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Immutable {

}
