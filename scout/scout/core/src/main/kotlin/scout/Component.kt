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

import scout.definition.AssociationKeys
import scout.definition.CollectionKeys
import scout.definition.ObjectKeys
import scout.scope.access.Accessor

/**
 * This class grants access to passed [scope] by protected inline methods.
 * Protected inline methods guarantees restricted access to dependencies
 * from this class inheritors. Signatures of protected methods coincide
 * with [Accessor] public methods.
 */
abstract class Component(val scope: Scope) {

    @PublishedApi
    internal val accessor = scope.accessor

    protected inline fun <reified T : Any> get(): T = accessor.get(ObjectKeys.create(T::class.java))

    protected inline fun <reified T : Any> getLazy(): Lazy<T> = accessor.getLazy(ObjectKeys.create(T::class.java))

    protected inline fun <reified T : Any> getProvider(): Provider<T> = accessor.getProvider(ObjectKeys.create(T::class.java))

    protected inline fun <reified T : Any> opt(): T? = accessor.opt(ObjectKeys.create(T::class.java))

    protected inline fun <reified T : Any> optLazy(): Lazy<T?> = accessor.optLazy(ObjectKeys.create(T::class.java))

    protected inline fun <reified T : Any> optProvider(): Provider<T?> = accessor.optProvider(ObjectKeys.create(T::class.java))

    protected inline fun <reified T : Any> collect(
        nonEmpty: Boolean = false
    ): List<T> = accessor.collect(CollectionKeys.create(T::class.java), nonEmpty)

    protected inline fun <reified T : Any> collectLazy(
        nonEmpty: Boolean = false
    ): Lazy<List<T>> = accessor.collectLazy(CollectionKeys.create(T::class.java), nonEmpty)

    protected inline fun <reified T : Any> collectProvider(
        nonEmpty: Boolean = false
    ): Provider<List<T>> = accessor.collectProvider(CollectionKeys.create(T::class.java), nonEmpty)

    protected inline fun <reified K : Any, reified V : Any> associate(
        nonEmpty: Boolean = false
    ): Map<K, V> = accessor.associate(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)

    protected inline fun <reified K : Any, reified V : Any> associateLazy(
        nonEmpty: Boolean = false
    ): Lazy<Map<K, V>> = accessor.associateLazy(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)

    protected inline fun <reified K : Any, reified V : Any> associateProvider(
        nonEmpty: Boolean = false
    ): Provider<Map<K, V>> = accessor.associateProvider(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)
}
