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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import scout.definition.AssociationKey
import scout.definition.AssociationKeys
import scout.definition.CollectionKey
import scout.definition.CollectionKeys
import scout.definition.ObjectKey
import scout.definition.ObjectKeys
import scout.scope.access.Accessor
import scout.scope.access.DirectAccessor
import scout.scope.access.Interceptor
import scout.scope.access.TrickyAccessor
import scout.utils.TestComponent

class InterceptorTest {

    @AfterEach
    fun tearDown() {
        Scout.Interceptors.clear()
        Scout.Optimizations.clear()
    }

    @Test
    fun `Check that interceptor called by TrickyAccessor`() {
        val before = TestBeforeInterceptor()
        val beforeLazy = TestBeforeLazyInterceptor()
        val beforeProvider = TestBeforeProviderInterceptor()
        val after = TestAfterInterceptor()
        val afterLazy = TestAfterLazyInterceptor()
        val afterProvider = TestAfterProviderInterceptor()
        Scout.Interceptors.register(before)
        Scout.Interceptors.register(beforeLazy)
        Scout.Interceptors.register(beforeProvider)
        Scout.Interceptors.register(after)
        Scout.Interceptors.register(afterLazy)
        Scout.Interceptors.register(afterProvider)
        val scope = scope("") {
            factory<String> { "test-string" }
            element<String> { "foo" }
            mapping<String, String> { "ru" to "RU" }
        }
        val accessor = TrickyAccessor(scope)
        accessor.get<String>()
        assertThat(before.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(after.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        accessor.getLazy<String>()
        assertThat(beforeLazy.getLazyRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(afterLazy.getLazyRequests).containsExactly(ObjectKeys.create(String::class.java))
        accessor.getProvider<String>()
        assertThat(beforeProvider.getProviderRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(afterProvider.getProviderRequests).containsExactly(ObjectKeys.create(String::class.java))
        accessor.opt<String>()
        assertThat(before.optRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(after.optRequests).containsExactly(ObjectKeys.create(String::class.java))
        accessor.optLazy<String>()
        assertThat(beforeLazy.optLazyRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(afterLazy.optLazyRequests).containsExactly(ObjectKeys.create(String::class.java))
        accessor.optProvider<String>()
        assertThat(beforeProvider.optProviderRequest).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(afterProvider.optProviderRequest).containsExactly(ObjectKeys.create(String::class.java))
        accessor.collect<String>()
        assertThat(before.collectRequests).containsExactly(CollectionKeys.create(String::class.java))
        assertThat(after.collectRequests).containsExactly(CollectionKeys.create(String::class.java))
        accessor.collectLazy<String>()
        assertThat(beforeLazy.collectLazyRequests).containsExactly(CollectionKeys.create(String::class.java))
        assertThat(afterLazy.collectLazyRequests).containsExactly(CollectionKeys.create(String::class.java))
        accessor.collectProvider<String>()
        assertThat(beforeProvider.collectProviderRequests).containsExactly(CollectionKeys.create(String::class.java))
        assertThat(afterProvider.collectProviderRequests).containsExactly(CollectionKeys.create(String::class.java))
        accessor.associate<String, String>()
        assertThat(before.associateRequests).containsExactly(AssociationKeys.create(String::class.java, String::class.java))
        assertThat(after.associateRequests).containsExactly(AssociationKeys.create(String::class.java, String::class.java))
        accessor.associateLazy<String, String>()
        assertThat(beforeLazy.associateLazyRequests).containsExactly(AssociationKeys.create(String::class.java, String::class.java))
        assertThat(afterLazy.associateLazyRequests).containsExactly(AssociationKeys.create(String::class.java, String::class.java))
        accessor.associateProvider<String, String>()
        assertThat(beforeProvider.associateProviderRequests).containsExactly(AssociationKeys.create(String::class.java, String::class.java))
        assertThat(afterProvider.associateProviderRequests).containsExactly(AssociationKeys.create(String::class.java, String::class.java))
    }

    @Test
    fun `Check that interceptors is not called by DirectAccessor`() {
        val before = TestBeforeInterceptor()
        val beforeLazy = TestBeforeLazyInterceptor()
        val beforeProvider = TestBeforeProviderInterceptor()
        val after = TestAfterInterceptor()
        val afterLazy = TestAfterLazyInterceptor()
        val afterProvider = TestAfterProviderInterceptor()
        Scout.Interceptors.register(before)
        Scout.Interceptors.register(beforeLazy)
        Scout.Interceptors.register(beforeProvider)
        Scout.Interceptors.register(after)
        Scout.Interceptors.register(afterLazy)
        Scout.Interceptors.register(afterProvider)
        val scope = scope("") {
            factory<String> { "test-string" }
            element<String> { "foo" }
            mapping<String, String> { "ru" to "RU" }
        }
        val accessor = DirectAccessor(scope)
        accessor.get<String>()
        assertThat(before.getRequests).hasSize(0)
        assertThat(after.getRequests).hasSize(0)
        accessor.getLazy<String>()
        assertThat(beforeLazy.getLazyRequests).hasSize(0)
        assertThat(afterLazy.getLazyRequests).hasSize(0)
        accessor.getProvider<String>()
        assertThat(beforeProvider.getProviderRequests).hasSize(0)
        assertThat(afterProvider.getProviderRequests).hasSize(0)
        accessor.opt<String>()
        assertThat(before.optRequests).hasSize(0)
        assertThat(after.optRequests).hasSize(0)
        accessor.optLazy<String>()
        assertThat(beforeLazy.optLazyRequests).hasSize(0)
        assertThat(afterLazy.optLazyRequests).hasSize(0)
        accessor.optProvider<String>()
        assertThat(beforeProvider.optProviderRequest).hasSize(0)
        assertThat(afterProvider.optProviderRequest).hasSize(0)
        accessor.collect<String>()
        assertThat(before.collectRequests).hasSize(0)
        assertThat(after.collectRequests).hasSize(0)
        accessor.collectLazy<String>()
        assertThat(beforeLazy.collectLazyRequests).hasSize(0)
        assertThat(afterLazy.collectLazyRequests).hasSize(0)
        accessor.collectProvider<String>()
        assertThat(beforeProvider.collectProviderRequests).hasSize(0)
        assertThat(afterProvider.collectProviderRequests).hasSize(0)
        accessor.associate<String, String>()
        assertThat(before.associateRequests).hasSize(0)
        assertThat(after.associateRequests).hasSize(0)
        accessor.associateLazy<String, String>()
        assertThat(beforeLazy.associateLazyRequests).hasSize(0)
        assertThat(afterLazy.associateLazyRequests).hasSize(0)
        accessor.associateProvider<String, String>()
        assertThat(beforeProvider.associateProviderRequests).hasSize(0)
        assertThat(afterProvider.associateProviderRequests).hasSize(0)
    }

    @Test
    fun `Check that interceptors is not called after unregistering`() {
        val before = TestBeforeInterceptor()
        val beforeLazy = TestBeforeLazyInterceptor()
        val beforeProvider = TestBeforeProviderInterceptor()
        val after = TestAfterInterceptor()
        val afterLazy = TestAfterLazyInterceptor()
        val afterProvider = TestAfterProviderInterceptor()
        Scout.Interceptors.register(before)
        Scout.Interceptors.register(beforeLazy)
        Scout.Interceptors.register(beforeProvider)
        Scout.Interceptors.register(after)
        Scout.Interceptors.register(afterLazy)
        Scout.Interceptors.register(afterProvider)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        Scout.Interceptors.unregister(before)
        Scout.Interceptors.unregister(beforeLazy)
        Scout.Interceptors.unregister(beforeProvider)
        Scout.Interceptors.unregister(after)
        Scout.Interceptors.unregister(afterLazy)
        Scout.Interceptors.unregister(afterProvider)
        val accessor = TrickyAccessor(scope)
        accessor.get<String>()
        accessor.getLazy<String>()
        accessor.getProvider<String>()
        assertThat(before.getRequests).hasSize(0)
        assertThat(beforeLazy.getLazyRequests).hasSize(0)
        assertThat(beforeProvider.getProviderRequests).hasSize(0)
        assertThat(after.getRequests).hasSize(0)
        assertThat(afterLazy.getLazyRequests).hasSize(0)
        assertThat(afterProvider.getProviderRequests).hasSize(0)
    }

    @Test
    fun `Check that interceptor is not called by Component if interceptors disabled`() {
        Scout.Optimizations.disableInterceptors()
        val before = TestBeforeInterceptor()
        val beforeLazy = TestBeforeLazyInterceptor()
        val beforeProvider = TestBeforeProviderInterceptor()
        val after = TestAfterInterceptor()
        val afterLazy = TestAfterLazyInterceptor()
        val afterProvider = TestAfterProviderInterceptor()
        Scout.Interceptors.register(before)
        Scout.Interceptors.register(beforeLazy)
        Scout.Interceptors.register(beforeProvider)
        Scout.Interceptors.register(after)
        Scout.Interceptors.register(afterLazy)
        Scout.Interceptors.register(afterProvider)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        component.getOrThrow<String>()
        assertThat(before.getRequests).hasSize(0)
        assertThat(after.getRequests).hasSize(0)
        component.getLazyOrThrow<String>()
        assertThat(beforeLazy.getLazyRequests).hasSize(0)
        assertThat(afterLazy.getLazyRequests).hasSize(0)
        component.getProviderOrThrow<String>()
        assertThat(beforeProvider.getProviderRequests).hasSize(0)
        assertThat(afterProvider.getProviderRequests).hasSize(0)
    }

    @Test
    fun `Check that interceptor is not called if Scout#applyInterceptors=false`() {
        val before = TestBeforeInterceptor()
        val beforeLazy = TestBeforeLazyInterceptor()
        val beforeProvider = TestBeforeProviderInterceptor()
        val after = TestAfterInterceptor()
        val afterLazy = TestAfterLazyInterceptor()
        val afterProvider = TestAfterProviderInterceptor()
        Scout.Interceptors.register(before)
        Scout.Interceptors.register(beforeLazy)
        Scout.Interceptors.register(beforeProvider)
        Scout.Interceptors.register(after)
        Scout.Interceptors.register(afterLazy)
        Scout.Interceptors.register(afterProvider)
        Scout.Interceptors.applyInterceptors = false
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        component.getOrThrow<String>()
        assertThat(before.getRequests).hasSize(0)
        assertThat(after.getRequests).hasSize(0)
        component.getLazyOrThrow<String>()
        assertThat(beforeLazy.getLazyRequests).hasSize(0)
        assertThat(afterLazy.getLazyRequests).hasSize(0)
        component.getProviderOrThrow<String>()
        assertThat(beforeProvider.getProviderRequests).hasSize(0)
        assertThat(afterProvider.getProviderRequests).hasSize(0)
    }

    @Test
    fun `Check that all registered interceptors called`() {
        val before1 = TestBeforeInterceptor()
        val before2 = TestBeforeInterceptor()
        val before3 = TestBeforeInterceptor()
        val after1 = TestAfterInterceptor()
        val after2 = TestAfterInterceptor()
        val after3 = TestAfterInterceptor()
        Scout.Interceptors.register(before1)
        Scout.Interceptors.register(before2)
        Scout.Interceptors.register(before3)
        Scout.Interceptors.register(after1)
        Scout.Interceptors.register(after2)
        Scout.Interceptors.register(after3)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        component.getOrThrow<String>()
        assertThat(before1.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(before2.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(before3.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(after1.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(after2.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(after3.getRequests).containsExactly(ObjectKeys.create(String::class.java))
    }

    @Test
    fun `Check that intercepting not interrupts with first not-none outcome`() {
        val interceptor1 = TestBeforeInterceptor().apply { getResult = "ts1" }
        val interceptor2 = TestBeforeInterceptor().apply { getResult = "ts2" }
        val interceptor3 = TestBeforeInterceptor()
        Scout.Interceptors.register(interceptor1)
        Scout.Interceptors.register(interceptor2)
        Scout.Interceptors.register(interceptor3)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        val result = component.getOrThrow<String>()
        assertThat(interceptor1.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(interceptor2.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(interceptor3.getRequests).containsExactly(ObjectKeys.create(String::class.java))
        assertThat(result).isEqualTo("ts2")
    }

    @Test
    fun `Check that TrickyAccessor returns value from before interceptor`() {
        val interceptor = TestBeforeInterceptor().apply { getResult = "ts" }
        Scout.Interceptors.register(interceptor)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        val result = component.getOrThrow<String>()
        assertThat(result).isEqualTo("ts")
    }

    @Test
    fun `Check that TrickyAccessor returns value from after interceptor`() {
        val interceptor = TestAfterInterceptor().apply { getResult = { "ts" } }
        Scout.Interceptors.register(interceptor)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        val result = component.getOrThrow<String>()
        assertThat(result).isEqualTo("ts")
    }

    @Test
    fun `Check that TrickyInterceptor returns last intercepted value`() {
        val before1 = TestBeforeInterceptor().apply { getResult = "ts1" }
        val before2 = TestBeforeInterceptor().apply { getResult = "ts2" }
        val after1 = TestAfterInterceptor().apply { getResult = { "ts3" } }
        val after2 = TestAfterInterceptor().apply { getResult = { "ts4" } }
        Scout.Interceptors.register(before1)
        Scout.Interceptors.register(before2)
        Scout.Interceptors.register(after1)
        Scout.Interceptors.register(after2)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        val result = component.getOrThrow<String>()
        assertThat(result).isEqualTo("ts4")
    }

    @Test
    fun `Check that Scout#Interceptors#applyInterceptors is false by default`() {
        assertThat(Scout.Interceptors.applyInterceptors).isEqualTo(false)
    }

    @Test
    fun `Check that Scout#Interceptors#applyInterceptors switches with registering and unregistering`() {
        val interceptor = TestBeforeInterceptor()
        Scout.Interceptors.register(interceptor)
        assertThat(Scout.Interceptors.applyInterceptors).isEqualTo(true)
        Scout.Interceptors.unregister(interceptor)
        assertThat(Scout.Interceptors.applyInterceptors).isEqualTo(false)
    }

    @Test
    fun `Check that Scout#Interceptors#applyInterceptors switches to false on clearing`() {
        val interceptor = TestBeforeInterceptor()
        Scout.Interceptors.register(interceptor)
        assertThat(Scout.Interceptors.applyInterceptors).isEqualTo(true)
        Scout.Interceptors.clear()
        assertThat(Scout.Interceptors.applyInterceptors).isEqualTo(false)
    }

    @Test
    fun `Check that Scout#Interceptors#register registers all subtypes of interceptor`() {
        var beforeCounter = 0
        var afterCounter = 0
        val interceptor = object : Interceptor.Before, Interceptor.After {
            override fun get(key: ObjectKey, accessor: Accessor) {
                beforeCounter += 1
            }

            override fun opt(key: ObjectKey, accessor: Accessor) {
                beforeCounter += 1
            }

            override fun collect(key: CollectionKey, accessor: Accessor) {
                beforeCounter += 1
            }

            override fun associate(key: AssociationKey, accessor: Accessor) {
                beforeCounter += 1
            }

            override fun get(key: ObjectKey, accessor: Accessor, result: Any): Any {
                afterCounter += 1
                return result
            }

            override fun opt(key: ObjectKey, accessor: Accessor, result: Any?): Any? {
                afterCounter += 1
                return result
            }

            override fun collect(key: CollectionKey, accessor: Accessor, result: List<Any>): List<Any> {
                afterCounter += 1
                return result
            }

            override fun associate(key: AssociationKey, accessor: Accessor, result: Map<*, *>): Map<*, *> {
                afterCounter += 1
                return result
            }
        }
        Scout.Interceptors.register(interceptor)
        val scope = scope("") {
            factory<String> { "test-string" }
        }
        val component = TestComponent(scope)
        component.getOrThrow<String>()
        assertThat(beforeCounter).isEqualTo(1)
        assertThat(afterCounter).isEqualTo(1)
    }

    private class TestBeforeInterceptor : Interceptor.Before {
        var getRequests = mutableListOf<ObjectKey>()
        var getResult: Any = Unit
        override fun get(key: ObjectKey, accessor: Accessor): Any {
            getRequests += key
            return getResult
        }

        var optRequests = mutableListOf<ObjectKey>()
        var optResult: Any? = Unit
        override fun opt(key: ObjectKey, accessor: Accessor): Any? {
            optRequests += key
            return optResult
        }

        var collectRequests = mutableListOf<CollectionKey>()
        var collectResult = Unit
        override fun collect(key: CollectionKey, accessor: Accessor): Any {
            collectRequests += key
            return collectResult
        }

        var associateRequests = mutableListOf<AssociationKey>()
        var associateResult = Unit
        override fun associate(key: AssociationKey, accessor: Accessor): Any {
            associateRequests += key
            return associateResult
        }
    }

    private class TestBeforeLazyInterceptor : Interceptor.BeforeLazy {

        var getLazyRequests = mutableListOf<ObjectKey>()
        var getLazyResult = Unit
        override fun getLazy(key: ObjectKey, accessor: Accessor): Any {
            getLazyRequests += key
            return getLazyResult
        }

        var optLazyRequests = mutableListOf<ObjectKey>()
        var optLazyResult = Unit
        override fun optLazy(key: ObjectKey, accessor: Accessor): Any {
            optLazyRequests += key
            return optLazyResult
        }

        var collectLazyRequests = mutableListOf<CollectionKey>()
        var collectLazyResult = Unit
        override fun collectLazy(key: CollectionKey, accessor: Accessor): Any {
            collectLazyRequests += key
            return collectLazyResult
        }

        var associateLazyRequests = mutableListOf<AssociationKey>()
        var associateLazyResult = Unit
        override fun associateLazy(key: AssociationKey, accessor: Accessor): Any {
            associateLazyRequests += key
            return associateLazyResult
        }
    }

    private class TestBeforeProviderInterceptor : Interceptor.BeforeProvider {

        var getProviderRequests = mutableListOf<ObjectKey>()
        var getProviderResult = Unit
        override fun getProvider(key: ObjectKey, accessor: Accessor): Any {
            getProviderRequests += key
            return getProviderResult
        }

        var optProviderRequest = mutableListOf<ObjectKey>()
        var optProviderResult = Unit
        override fun optProvider(key: ObjectKey, accessor: Accessor): Any {
            optProviderRequest += key
            return optProviderResult
        }

        var collectProviderRequests = mutableListOf<CollectionKey>()
        var collectProviderResult = Unit
        override fun collectProvider(key: CollectionKey, accessor: Accessor): Any {
            collectProviderRequests += key
            return collectProviderResult
        }

        var associateProviderRequests = mutableListOf<AssociationKey>()
        var associateProviderResult = Unit
        override fun associateProvider(key: AssociationKey, accessor: Accessor): Any {
            associateProviderRequests += key
            return associateProviderResult
        }
    }

    private class TestAfterInterceptor : Interceptor.After {
        var getRequests = mutableListOf<ObjectKey>()
        var getResult: (Any) -> Any = { it }
        override fun get(key: ObjectKey, accessor: Accessor, result: Any): Any {
            getRequests += key
            return getResult(result)
        }

        var optRequests = mutableListOf<ObjectKey>()
        var optResult: (Any?) -> Any? = { it }
        override fun opt(key: ObjectKey, accessor: Accessor, result: Any?): Any? {
            optRequests += key
            return optResult(result)
        }

        var collectRequests = mutableListOf<CollectionKey>()
        var collectResult: (List<Any>) -> List<Any> = { it }
        override fun collect(key: CollectionKey, accessor: Accessor, result: List<Any>): List<Any> {
            collectRequests += key
            return collectResult(result)
        }

        var associateRequests = mutableListOf<AssociationKey>()
        var associateResult: (Map<*, *>) -> Map<*, *> = { it }
        override fun associate(key: AssociationKey, accessor: Accessor, result: Map<*, *>): Map<*, *> {
            associateRequests += key
            return associateResult(result)
        }
    }

    private class TestAfterLazyInterceptor : Interceptor.AfterLazy {

        var getLazyRequests = mutableListOf<ObjectKey>()
        var getLazyResult: (Lazy<Any>) -> Lazy<Any> = { it }
        override fun getLazy(key: ObjectKey, accessor: Accessor, result: Lazy<Any>): Lazy<Any> {
            getLazyRequests += key
            return getLazyResult(result)
        }

        var optLazyRequests = mutableListOf<ObjectKey>()
        var optLazyResult: (Lazy<Any?>) -> Lazy<Any?> = { it }
        override fun optLazy(key: ObjectKey, accessor: Accessor, result: Lazy<Any?>): Lazy<Any?> {
            optLazyRequests += key
            return optLazyResult(result)
        }

        var collectLazyRequests = mutableListOf<CollectionKey>()
        var collectLazyResult: (Lazy<List<Any>>) -> Lazy<List<Any>> = { it }
        override fun collectLazy(key: CollectionKey, accessor: Accessor, result: Lazy<List<Any>>): Lazy<List<Any>> {
            collectLazyRequests += key
            return collectLazyResult(result)
        }

        var associateLazyRequests = mutableListOf<AssociationKey>()
        var associateLazyResult: (Lazy<Map<*, *>>) -> Lazy<Map<*, *>> = { it }
        override fun associateLazy(key: AssociationKey, accessor: Accessor, result: Lazy<Map<*, *>>): Lazy<Map<*, *>> {
            associateLazyRequests += key
            return associateLazyResult(result)
        }
    }

    private class TestAfterProviderInterceptor : Interceptor.AfterProvider {

        var getProviderRequests = mutableListOf<ObjectKey>()
        var getProviderResult: (Provider<Any>) -> Provider<Any> = { it }
        override fun getProvider(key: ObjectKey, accessor: Accessor, result: Provider<Any>): Provider<Any> {
            getProviderRequests += key
            return getProviderResult(result)
        }

        var optProviderRequest = mutableListOf<ObjectKey>()
        var optProviderResult: (Provider<Any?>) -> Provider<Any?> = { it }
        override fun optProvider(key: ObjectKey, accessor: Accessor, result: Provider<Any?>): Provider<Any?> {
            optProviderRequest += key
            return optProviderResult(result)
        }

        var collectProviderRequests = mutableListOf<CollectionKey>()
        var collectProviderResult: (Provider<List<Any>>) -> Provider<List<Any>> = { it }
        override fun collectProvider(key: CollectionKey, accessor: Accessor, result: Provider<List<Any>>): Provider<List<Any>> {
            collectProviderRequests += key
            return collectProviderResult(result)
        }

        var associateProviderRequests = mutableListOf<AssociationKey>()
        var associateProviderResult: (Provider<Map<*, *>>) -> Provider<Map<*, *>> = { it }
        override fun associateProvider(key: AssociationKey, accessor: Accessor, result: Provider<Map<*, *>>): Provider<Map<*, *>> {
            associateProviderRequests += key
            return associateProviderResult(result)
        }
    }
}
