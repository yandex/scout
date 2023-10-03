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
package scout.utils

import scout.Component
import scout.Provider
import scout.Scope
import scout.definition.AssociationKeys
import scout.definition.CollectionKeys
import scout.definition.ObjectKeys
import scout.scope.access.Accessor

class TestComponent(scope: Scope) : Component(scope) {

    val access: Accessor by lazy {
        val field = Component::class.java.getDeclaredField("accessor")
        field.isAccessible = true
        field.get(this) as Accessor
    }

    inline fun <reified T : Any> getOrThrow(): T =
        access.get(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> getLazyOrThrow(): Lazy<T> =
        access.getLazy(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> getProviderOrThrow(): Provider<T> =
        access.getProvider(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> optOrThrow(): T? =
        access.opt(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> optLazyOrThrow(): Lazy<T?> =
        access.optLazy(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> optProviderOrThrow(): Provider<T?> =
        access.optProvider(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> collectOrThrow(nonEmpty: Boolean = false): List<T> =
        access.collect(CollectionKeys.create(T::class.java), nonEmpty)

    inline fun <reified T : Any> collectLazyOrThrow(nonEmpty: Boolean = false): Lazy<List<T>> =
        access.collectLazy(CollectionKeys.create(T::class.java), nonEmpty)

    inline fun <reified T : Any> collectProviderOrThrow(nonEmpty: Boolean = false): Provider<List<T>> =
        access.collectProvider(CollectionKeys.create(T::class.java), nonEmpty)

    inline fun <reified K : Any, reified V : Any> associateOrThrow(nonEmpty: Boolean = false): Map<K, V> =
        access.associate(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)

    inline fun <reified K : Any, reified V : Any> associateLazyOrThrow(nonEmpty: Boolean = false): Lazy<Map<K, V>> =
        access.associateLazy(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)

    inline fun <reified K : Any, reified V : Any> associateProviderOrThrow(nonEmpty: Boolean = false): Provider<Map<K, V>> =
        access.associateProvider(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)
}
