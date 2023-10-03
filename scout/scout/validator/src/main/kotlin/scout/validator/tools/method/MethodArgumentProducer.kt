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
 * Allows to substitute arguments for constructor while instance creation.
 * Selects argument value based on:
 * - receiver instance
 * - invoking method
 * - parameter type
 */
fun interface MethodArgumentProducer<T> {

    /**
     * Produce argument for method call.
     */
    fun produce(
        receiver: T,
        method: Method,
        parameterType: Class<*>
    ): Any?
}
