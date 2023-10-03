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
import scout.Scope
import scout.Scout
import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey

/**
 * This class delegates accessor calls to interceptors before
 * direct access to scope content.
 */
internal class TrickyAccessor(scope: Scope) : Accessor(scope) {

    /**
     * Direct accessor to prevent recursion problems
     * during interceptor calls.
     */
    private val direct = DirectAccessor(scope)

    private inline fun <B, A, T> intercept(
        origin: () -> T,
        before: (B) -> Any?,
        after: (A, T) -> Any?,
        interceptors: InterceptorRegistry.Snapshot<B, A>
    ): T {
        var result: Any? = Unit
        for (interceptor in interceptors.before) {
            val outcome = before(interceptor)
            if (outcome !== Unit) {
                result = outcome
            }
        }
        if (result === Unit) {
            result = origin()
        }
        for (interceptor in interceptors.after) {
            @Suppress("UNCHECKED_CAST")
            result = after(interceptor, result as T) as T
        }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    override fun <T : Any> get(key: ObjectKey): T {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.get(key) },
                before = { interceptor -> interceptor.get(key, direct) },
                after = { interceptor, result -> interceptor.get(key, direct, result) },
                Scout.Interceptors.forRegular
            )
        }
        return super.get(key)
    }

    override fun <T : Any> getLazy(key: ObjectKey): Lazy<T> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.getLazy(key) },
                before = { interceptor -> interceptor.getLazy(key, direct) },
                after = { interceptor, result -> interceptor.getLazy(key, direct, result) },
                Scout.Interceptors.forLazy
            )
        }
        return super.getLazy(key)
    }

    override fun <T : Any> getProvider(key: ObjectKey): Provider<T> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.getProvider(key) },
                before = { interceptor -> interceptor.getProvider(key, direct) },
                after = { interceptor, result -> interceptor.getProvider(key, direct, result) },
                Scout.Interceptors.forProvider
            )
        }
        return super.getProvider(key)
    }

    override fun <T : Any> opt(key: ObjectKey): T? {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.opt(key) },
                before = { interceptor -> interceptor.opt(key, direct) },
                after = { interceptor, result -> interceptor.opt(key, direct, result) },
                Scout.Interceptors.forRegular
            )
        }
        return super.opt(key)
    }

    override fun <T : Any> optLazy(key: ObjectKey): Lazy<T?> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.optLazy(key) },
                before = { interceptor -> interceptor.optLazy(key, direct) },
                after = { interceptor, result -> interceptor.optLazy(key, direct, result) },
                Scout.Interceptors.forLazy
            )
        }
        return super.optLazy(key)
    }

    override fun <T : Any> optProvider(key: ObjectKey): Provider<T?> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.optProvider(key) },
                before = { interceptor -> interceptor.optProvider(key, direct) },
                after = { interceptor, result -> interceptor.optProvider(key, direct, result) },
                Scout.Interceptors.forProvider
            )
        }
        return super.optProvider(key)
    }

    override fun <T : Any> collect(key: CollectionKey, nonEmpty: Boolean): List<T> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.collect(key, nonEmpty) },
                before = { interceptor -> interceptor.collect(key, direct) },
                after = { interceptor, result -> interceptor.collect(key, direct, result) },
                Scout.Interceptors.forRegular
            )
        }
        return super.collect(key, nonEmpty)
    }

    override fun <T : Any> collectLazy(key: CollectionKey, nonEmpty: Boolean): Lazy<List<T>> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.collectLazy(key, nonEmpty) },
                before = { interceptor -> interceptor.collectLazy(key, direct) },
                after = { interceptor, result -> interceptor.collectLazy(key, direct, result) },
                Scout.Interceptors.forLazy
            )
        }
        return super.collectLazy(key, nonEmpty)
    }

    override fun <T : Any> collectProvider(key: CollectionKey, nonEmpty: Boolean): Provider<List<T>> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.collectProvider(key, nonEmpty) },
                before = { interceptor -> interceptor.collectProvider(key, direct) },
                after = { interceptor, result -> interceptor.collectProvider(key, direct, result) },
                Scout.Interceptors.forProvider
            )
        }
        return super.collectProvider(key, nonEmpty)
    }

    override fun <K : Any, V : Any> associate(key: AssociationKey, nonEmpty: Boolean): Map<K, V> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.associate(key, nonEmpty) },
                before = { interceptor -> interceptor.associate(key, direct) },
                after = { interceptor, result -> interceptor.associate(key, direct, result) },
                Scout.Interceptors.forRegular
            )
        }
        return super.associate(key, nonEmpty)
    }

    override fun <K : Any, V : Any> associateLazy(key: AssociationKey, nonEmpty: Boolean): Lazy<Map<K, V>> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.associateLazy(key, nonEmpty) },
                before = { interceptor -> interceptor.associateLazy(key, direct) },
                after = { interceptor, result -> interceptor.associateLazy(key, direct, result) },
                Scout.Interceptors.forLazy
            )
        }
        return super.associateLazy(key, nonEmpty)
    }

    override fun <K : Any, V : Any> associateProvider(key: AssociationKey, nonEmpty: Boolean): Provider<Map<K, V>> {
        if (Scout.Interceptors.applyInterceptors) {
            return intercept(
                origin = { super.associateProvider(key, nonEmpty) },
                before = { interceptor -> interceptor.associateProvider(key, direct) },
                after = { interceptor, result -> interceptor.associateProvider(key, direct, result) },
                Scout.Interceptors.forProvider
            )
        }
        return super.associateProvider(key, nonEmpty)
    }
}
