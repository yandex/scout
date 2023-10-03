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
package scout.definition

import scout.factory.strategy.FactoryInstanceFactory
import scout.factory.InstanceFactory
import scout.factory.strategy.ReusableInstanceFactory
import scout.factory.strategy.SingleInstanceFactory

/**
 * Holds factories registered while scope building process.
 */
abstract class Registry {

    @PublishedApi
    internal abstract fun saveObjectMapping(
        definitionKey: ObjectKey,
        factory: InstanceFactory<*>,
        allowOverride: Boolean,
    )

    @PublishedApi
    internal abstract fun saveCollectionMapping(
        definitionKey: CollectionKey,
        factory: InstanceFactory<*>,
    )

    @PublishedApi
    internal abstract fun saveAssociationMapping(
        definitionKey: AssociationKey,
        factory: InstanceFactory<out Pair<Any, Any>>,
    )

    /**
     * Registers regular factory for instance of type T
     * with [definition] lambda as a creation method.
     */
    inline fun <reified T> factory(
        allowOverride: Boolean = false,
        crossinline definition: Definition<T, T>,
    ) {
        val key = ObjectKeys.create(T::class.java)
        val factory = FactoryInstanceFactory(definition)
        saveObjectMapping(key, factory, allowOverride)
    }

    /**
     * Registers singleton factory for instance of type T
     * with [definition] lambda as a creation method.
     */
    inline fun <reified T> singleton(
        allowOverride: Boolean = false,
        crossinline definition: Definition<T, T>,
    ) {
        val key = ObjectKeys.create(T::class.java)
        val factory = SingleInstanceFactory(definition)
        saveObjectMapping(key, factory, allowOverride)
    }

    /**
     * Registers reusable factory for instance of type T
     * with [definition] lambda as a creation method.
     */
    inline fun <reified T> reusable(
        allowOverride: Boolean = false,
        crossinline definition: Definition<T, T>,
    ) {
        val key = ObjectKeys.create(T::class.java)
        val factory = ReusableInstanceFactory(definition)
        saveObjectMapping(key, factory, allowOverride)
    }

    /**
     * Registers factory of element for List<T>
     * with [definition] lambda as creation method.
     */
    inline fun <reified T> element(
        crossinline definition: Definition<T, T>,
    ) {
        val definitionKey = CollectionKeys.create(T::class.java)
        val factory = FactoryInstanceFactory(definition)
        saveCollectionMapping(definitionKey, factory)
    }

    /**
     * Registers factory of mapping for Map<K, V>
     * with [definition] lambda as creation method.
     */
    inline fun <reified K : Any, reified V : Any> mapping(
        crossinline definition: Definition<K, Pair<K, V>>,
    ) {
        val definitionKey = AssociationKeys.create(K::class.java, V::class.java)
        val factory = FactoryInstanceFactory(definition)
        saveAssociationMapping(definitionKey, factory)
    }
}
