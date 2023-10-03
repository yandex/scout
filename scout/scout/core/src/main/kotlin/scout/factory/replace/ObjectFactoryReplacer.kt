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
package scout.factory.replace

import scout.Provider
import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.Definition
import scout.definition.ObjectKey
import scout.definition.ObjectKeys
import scout.factory.InstanceFactory
import scout.factory.strategy.FactoryInstanceFactory
import scout.factory.strategy.ReusableInstanceFactory
import scout.factory.strategy.SingleInstanceFactory
import scout.scope.access.Accessor
import scout.scope.access.Interceptor

/**
 * Allows to replace registered instance factories with
 * specified replacements.
 *
 * Option 1:
 * Use [Builder] instance to configure replacements.
 * ```
 * ObjectFactoryReplacer.Builder()
 *     .factory<String> { "Hello, world!" }
 *     .factory<Int> { 42 }
 *     .build()
 * ```
 *
 * Option 2:
 * Use lambda with [Builder] context to configure replacements.
 * ```
 * ObjectFactoryReplacer {
 *     factory<String> { "Hello, world!" }
 *     factory<Int> { 42 }
 * }
 * ```
 */
class ObjectFactoryReplacer(
    private val stubs: Map<ObjectKey, InstanceFactory<*>>
) : Interceptor.Before, Interceptor.BeforeLazy, Interceptor.BeforeProvider {

    override fun get(key: ObjectKey, accessor: Accessor): Any {
        val stub = stubs[key]
        if (stub != null && accessor.scope.containsObjectFactory(key)) {
            return stub.get(accessor) as Any
        }
        return Unit
    }

    override fun getLazy(key: ObjectKey, accessor: Accessor): Any {
        val stub = stubs[key]
        if (stub != null && accessor.scope.containsObjectFactory(key)) {
            return lazy { stub.get(accessor) }
        }
        return Unit
    }

    override fun getProvider(key: ObjectKey, accessor: Accessor): Any {
        val stub = stubs[key]
        if (stub != null && accessor.scope.containsObjectFactory(key)) {
            return Provider { stub.get(accessor) }
        }
        return Unit
    }

    override fun opt(key: ObjectKey, accessor: Accessor): Any? {
        val stub = stubs[key]
        if (stub != null && accessor.scope.containsObjectFactory(key)) {
            return stub.get(accessor)
        }
        return Unit
    }

    override fun optLazy(key: ObjectKey, accessor: Accessor): Any {
        val stub = stubs[key]
        if (stub != null && accessor.scope.containsObjectFactory(key)) {
            return lazy { stub.get(accessor) }
        }
        return Unit
    }

    override fun optProvider(key: ObjectKey, accessor: Accessor): Any {
        val stub = stubs[key]
        if (stub != null && accessor.scope.containsObjectFactory(key)) {
            return Provider { stub.get(accessor) }
        }
        return Unit
    }

    override fun collect(key: CollectionKey, accessor: Accessor) {}

    override fun collectLazy(key: CollectionKey, accessor: Accessor) {}

    override fun collectProvider(key: CollectionKey, accessor: Accessor) {}

    override fun associate(key: AssociationKey, accessor: Accessor) {}

    override fun associateLazy(key: AssociationKey, accessor: Accessor) {}

    override fun associateProvider(key: AssociationKey, accessor: Accessor) {}

    class Builder {

        private val stubs = mutableMapOf<ObjectKey, InstanceFactory<*>>()

        /**
         * Replace factory for type T to factory with definition.
         */
        inline fun <reified T : Any> factory(
            crossinline definition: Definition<T, T>
        ) = replace(ObjectKeys.create(T::class.java), FactoryInstanceFactory(definition))

        /**
         * Replace factory for type T to singleton with definition.
         */
        inline fun <reified T : Any> singleton(
            crossinline definition: Definition<T, T>
        ) = replace(ObjectKeys.create(T::class.java), SingleInstanceFactory(definition))

        /**
         * Replace factory for type T to reusable with definition.
         */
        inline fun <reified T : Any> reusable(
            crossinline definition: Definition<T, T>
        ) = replace(ObjectKeys.create(T::class.java), ReusableInstanceFactory(definition))

        /**
         * Replace factory by [key] to [factory].
         */
        @PublishedApi
        internal fun replace(
            key: ObjectKey,
            factory: InstanceFactory<*>
        ): Builder = apply { stubs[key] = factory }

        /**
         * Build [ObjectFactoryReplacer] instance.
         */
        fun build() = ObjectFactoryReplacer(stubs)
    }
}

/**
 * Allows to build [ObjectFactoryReplacer] in configuration-pattern manner.
 * ```
 * ObjectFactoryReplacer {
 *     factory<String> { "Hello, world!" }
 *     factory<Int> { 42 }
 * }
 * ```
 */
fun ObjectFactoryReplacer(configure: ObjectFactoryReplacer.Builder.() -> Unit): ObjectFactoryReplacer {
    val builder = ObjectFactoryReplacer.Builder()
    configure(builder)
    return builder.build()
}
