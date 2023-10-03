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
package scout.scope.access

import scout.Provider
import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey

/**
 * This interface defines intercepting protocol. Each [Accessor]'s
 * method can be intercepted by this interface implementer
 * with aim to extend dependency provision by extra logic.
 */
sealed interface Interceptor {

    /**
     * Intercepts access to scope before accessor call.
     *
     * Each method can return:
     * - Value that should be returned instead of scope content.
     * - [Unit] to signal that interceptor does not provide result.
     */
    interface Before : Interceptor {

        /**
         * Intercept [Accessor.get] before accessor call.
         */
        fun get(key: ObjectKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.opt] before accessor call.
         */
        fun opt(key: ObjectKey, accessor: Accessor): Any?

        /**
         * Intercept [Accessor.collect] before accessor call.
         */
        fun collect(key: CollectionKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.associate] before accessor call.
         */
        fun associate(key: AssociationKey, accessor: Accessor): Any
    }

    /**
     * Intercepts access to scope before accessor call.
     *
     * Each method can return:
     * - Value that should be returned instead of scope content.
     * - [Unit] to signal that interceptor does not provide result.
     */
    interface BeforeLazy : Interceptor {

        /**
         * Intercept [Accessor.getLazy] before accessor call.
         */
        fun getLazy(key: ObjectKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.optLazy] before accessor call.
         */
        fun optLazy(key: ObjectKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.collectLazy] before accessor call.
         */
        fun collectLazy(key: CollectionKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.associateLazy] before accessor call.
         */
        fun associateLazy(key: AssociationKey, accessor: Accessor): Any
    }

    /**
     * Intercepts access to scope before accessor call.
     *
     * Each method can return:
     * - Value that should be returned instead of scope content.
     * - [Unit] to signal that interceptor does not provide result.
     */
    interface BeforeProvider : Interceptor {

        /**
         * Intercept [Accessor.getProvider] before accessor call.
         */
        fun getProvider(key: ObjectKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.optProvider] before accessor call.
         */
        fun optProvider(key: ObjectKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.collectProvider] before accessor call.
         */
        fun collectProvider(key: CollectionKey, accessor: Accessor): Any

        /**
         * Intercept [Accessor.associateProvider] before accessor call.
         */
        fun associateProvider(key: AssociationKey, accessor: Accessor): Any
    }

    /**
     * Intercepts access to scope after accessor call.
     *
     * Each method can return:
     * - Value that should be returned instead of scope content.
     * - Original value passed to result method parameter.
     */
    interface After : Interceptor {

        /**
         * Intercept [Accessor.get] after accessor call.
         */
        fun get(key: ObjectKey, accessor: Accessor, result: Any): Any

        /**
         * Intercept [Accessor.opt] after accessor call.
         */
        fun opt(key: ObjectKey, accessor: Accessor, result: Any?): Any?

        /**
         * Intercept [Accessor.collect] after accessor call.
         */
        fun collect(key: CollectionKey, accessor: Accessor, result: List<Any>): List<Any>

        /**
         * Intercept [Accessor.associate] after accessor call.
         */
        fun associate(key: AssociationKey, accessor: Accessor, result: Map<*, *>): Map<*, *>
    }

    /**
     * Intercepts access to scope after accessor call.
     *
     * Each method can return:
     * - Value that should be returned instead of scope content.
     * - Original value passed to result method parameter.
     */
    interface AfterLazy : Interceptor {

        /**
         * Intercept [Accessor.getLazy] after accessor call.
         */
        fun getLazy(key: ObjectKey, accessor: Accessor, result: Lazy<Any>): Lazy<Any>

        /**
         * Intercept [Accessor.optLazy] after accessor call.
         */
        fun optLazy(key: ObjectKey, accessor: Accessor, result: Lazy<Any?>): Lazy<Any?>

        /**
         * Intercept [Accessor.collectLazy] after accessor call.
         */
        fun collectLazy(key: CollectionKey, accessor: Accessor, result: Lazy<List<Any>>): Lazy<List<Any>>

        /**
         * Intercept [Accessor.associateLazy] after accessor call.
         */
        fun associateLazy(key: AssociationKey, accessor: Accessor, result: Lazy<Map<*, *>>): Lazy<Map<*, *>>
    }

    /**
     * Intercepts access to scope after accessor call.
     *
     * Each method can return:
     * - Value that should be returned instead of scope content.
     * - Original value passed to result method parameter.
     */
    interface AfterProvider : Interceptor {

        /**
         * Intercept [Accessor.getProvider] after accessor call.
         */
        fun getProvider(key: ObjectKey, accessor: Accessor, result: Provider<Any>): Provider<Any>

        /**
         * Intercept [Accessor.optProvider] after accessor call.
         */
        fun optProvider(key: ObjectKey, accessor: Accessor, result: Provider<Any?>): Provider<Any?>

        /**
         * Intercept [Accessor.collectProvider] after accessor call.
         */
        fun collectProvider(key: CollectionKey, accessor: Accessor, result: Provider<List<Any>>): Provider<List<Any>>

        /**
         * Intercept [Accessor.associateProvider] after accessor call.
         */
        fun associateProvider(key: AssociationKey, accessor: Accessor, result: Provider<Map<*, *>>): Provider<Map<*, *>>
    }
}
