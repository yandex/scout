/*
 * Copyright 2023 Yandex LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scout.validator.tools.method

import java.lang.reflect.Method

/**
 * Allows to filter methods.
 */
fun interface MethodFilter {

    /**
     * Check if method fits filter.
     */
    fun matches(method: Method): Boolean

    companion object {
        /**
         * Filter that passes any method.
         */
        val any = MethodFilter { true }
    }
}
