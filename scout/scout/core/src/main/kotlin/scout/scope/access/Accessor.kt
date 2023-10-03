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

import scout.Scope
import scout.annotation.AccessorMarker
import scout.definition.AssociationKey
import scout.definition.AssociationKeys
import scout.definition.CollectionKey
import scout.definition.CollectionKeys
import scout.definition.ObjectKey
import scout.definition.ObjectKeys
import scout.Provider

/**
 * This class introduces the ability to get objects/collections/associations from specified scope.
 */
@AccessorMarker
sealed class Accessor(val scope: Scope) {

    inline fun <reified T : Any> get(): T =
        get(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> getLazy(): Lazy<T> =
        getLazy(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> getProvider(): Provider<T> =
        getProvider(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> opt(): T? =
        opt(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> optLazy(): Lazy<T?> =
        optLazy(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> optProvider(): Provider<T?> =
        optProvider(ObjectKeys.create(T::class.java))

    inline fun <reified T : Any> collect(
        nonEmpty: Boolean = false
    ): List<T> = collect(CollectionKeys.create(T::class.java), nonEmpty)

    inline fun <reified T : Any> collectLazy(
        nonEmpty: Boolean = false
    ): Lazy<List<T>> = collectLazy(CollectionKeys.create(T::class.java), nonEmpty)

    inline fun <reified T : Any> collectProvider(
        nonEmpty: Boolean = false
    ): Provider<List<T>> = collectProvider(CollectionKeys.create(T::class.java), nonEmpty)

    inline fun <reified K : Any, reified V : Any> associate(
        nonEmpty: Boolean = false
    ): Map<K, V> = associate(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)

    inline fun <reified K : Any, reified V : Any> associateProvider(
        nonEmpty: Boolean = false
    ): Provider<Map<K, V>> = associateProvider(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)

    inline fun <reified K : Any, reified V : Any> associateLazy(
        nonEmpty: Boolean = false
    ): Lazy<Map<K, V>> = associateLazy(AssociationKeys.create(K::class.java, V::class.java), nonEmpty)

    @PublishedApi
    internal open fun <T : Any> get(key: ObjectKey): T {
        @Suppress("UNCHECKED_CAST")
        return scope.getObject(key, required = true) as T
    }

    @PublishedApi
    internal open fun <T : Any> getLazy(key: ObjectKey): Lazy<T> {
        @Suppress("UNCHECKED_CAST")
        return scope.getObjectLazy(key, required = true) as Lazy<T>
    }

    @PublishedApi
    internal open fun <T : Any> getProvider(key: ObjectKey): Provider<T> {
        @Suppress("UNCHECKED_CAST")
        return scope.getObjectProvider(key, required = true) as Provider<T>
    }

    @PublishedApi
    internal open fun <T : Any> opt(key: ObjectKey): T? {
        @Suppress("UNCHECKED_CAST")
        return scope.getObject(key, required = false) as T?
    }

    @PublishedApi
    internal open fun <T : Any> optLazy(key: ObjectKey): Lazy<T?> {
        @Suppress("UNCHECKED_CAST")
        return scope.getObjectLazy(key, required = false) as Lazy<T?>
    }

    @PublishedApi
    internal open fun <T : Any> optProvider(key: ObjectKey): Provider<T?> {
        @Suppress("UNCHECKED_CAST")
        return scope.getObjectProvider(key, required = false) as Provider<T?>
    }

    @PublishedApi
    internal open fun <T : Any> collect(key: CollectionKey, nonEmpty: Boolean): List<T> {
        @Suppress("UNCHECKED_CAST")
        return scope.getCollection(key, required = nonEmpty) as List<T>
    }

    @PublishedApi
    internal open fun <T : Any> collectLazy(key: CollectionKey, nonEmpty: Boolean): Lazy<List<T>> {
        @Suppress("UNCHECKED_CAST")
        return scope.getCollectionLazy(key, nonEmpty = nonEmpty) as Lazy<List<T>>
    }

    @PublishedApi
    internal open fun <T : Any> collectProvider(key: CollectionKey, nonEmpty: Boolean): Provider<List<T>> {
        @Suppress("UNCHECKED_CAST")
        return scope.getCollectionProvider(key, nonEmpty = nonEmpty) as Provider<List<T>>
    }

    @PublishedApi
    internal open fun <K : Any, V : Any> associate(key: AssociationKey, nonEmpty: Boolean): Map<K, V> {
        @Suppress("UNCHECKED_CAST")
        return scope.getAssociation(key, required = nonEmpty) as Map<K, V>
    }

    @PublishedApi
    internal open fun <K : Any, V : Any> associateLazy(key: AssociationKey, nonEmpty: Boolean): Lazy<Map<K, V>> {
        @Suppress("UNCHECKED_CAST")
        return scope.getAssociationLazy(key, nonEmpty = nonEmpty) as Lazy<Map<K, V>>
    }

    @PublishedApi
    internal open fun <K : Any, V : Any> associateProvider(key: AssociationKey, nonEmpty: Boolean): Provider<Map<K, V>> {
        @Suppress("UNCHECKED_CAST")
        return scope.getAssociationProvider(key, nonEmpty = nonEmpty) as Provider<Map<K, V>>
    }
}
