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
package scout

import scout.logging.Logger
import scout.scope.access.Interceptor
import scout.scope.access.InterceptorRegistry
import scout.scope.builder.ScopeBuilder

/**
 * Global Scout configuration.
 */
object Scout {

    /**
     * Default logger for Scout related code.
     */
    var defaultLogger: Logger? = null
        private set

    /**
     * Set default logger for Scout related code.
     */
    fun setDefaultLogger(logger: Logger?): Scout {
        defaultLogger = logger
        return this
    }

    /**
     * Global intercepting configuration.
     */
    object Interceptors {

        private val lock = Any()
        private var registry = InterceptorRegistry()

        /**
         * Optimization flag that allows to prevent
         * synchronized access to interceptor list
         * if there is no registered interceptors.
         */
        @Volatile
        internal var applyInterceptors = false

        /**
         * Get registered interceptors synchronously.
         */
        internal val forRegular get() = synchronized(lock) { registry.snapshotRegular() }

        /**
         * Get registered for lazy interceptors synchronously.
         */
        internal val forLazy get() = synchronized(lock) { registry.snapshotLazy() }

        /**
         * Get registered for provider interceptors synchronously.
         */
        internal val forProvider get() = synchronized(lock) { registry.snapshotProvider() }

        /**
         * Register interceptor.
         */
        fun register(interceptor: Interceptor): Interceptors {
            synchronized(lock) {
                registry.register(interceptor)
                if (!registry.isEmpty()) {
                    applyInterceptors = true
                }
            }
            defaultLogger?.warning {
                "Interceptor $interceptor is registered, " +
                        "performance may be dramatically reduced."
            }
            return this
        }

        /**
         * Unregister interceptor.
         */
        fun unregister(interceptor: Interceptor): Interceptors {
            synchronized(lock) {
                registry.unregister(interceptor)
                if (registry.isEmpty()) {
                    applyInterceptors = false
                }
            }
            return this
        }

        /**
         * Clears all registered interceptors.
         */
        fun clear(): Interceptors {
            synchronized(lock) {
                registry.clear()
                applyInterceptors = false
            }
            return this
        }
    }

    /**
     * Global optimizations configuration.
     */
    object Optimizations {

        internal var disableInterceptors = false

        /**
         * Completely disables intercepting mechanism
         * to avoid interceptor processing overhead costs.
         */
        fun disableInterceptors(disable: Boolean = true): Optimizations {
            disableInterceptors = disable
            return this
        }

        /**
         * Clears all optimizations.
         */
        fun clear(): Optimizations {
            disableInterceptors = false
            return this
        }
    }

    object ThreadSafety {

        @PublishedApi
        internal var scopeBuilderMode: ScopeBuilder.ThreadSafetyMode = ScopeBuilder.ThreadSafetyMode.Unsafe

        /**
         * Sed default [ScopeBuilder.ThreadSafetyMode] (thread-unsafe, thread-safe, confined).
         */
        fun setDefaultScopeBuilderMode(mode: ScopeBuilder.ThreadSafetyMode): ThreadSafety {
            scopeBuilderMode = mode
            return this
        }
    }
}
