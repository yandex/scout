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
package scout.scope.builder

import scout.Scope
import scout.definition.Registry
import scout.logging.Logger

sealed class ScopeBuilder : Registry() {

    abstract fun dependsOn(parent: Scope)

    abstract fun logger(logger: Logger?)

    abstract fun build(): Scope

    enum class ThreadSafetyMode {
        /**
         * Most performant mode with no synchronization and no checks
         */
        Unsafe,

        /**
         * In this mode each method invocation will be checked to be performed by exact same thread. Exception will be
         * thrown otherwise. Dramatically slows down performance, so don't use this mode for release builds.
         */
        Confined,

        /**
         * Thread-safe mode with all methods being synchronized internally
         */
        Synchronized,
    }
}
