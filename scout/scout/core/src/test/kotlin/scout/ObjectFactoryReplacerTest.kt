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
import scout.factory.replace.ObjectFactoryReplacer

class ObjectFactoryReplacerTest {

    @AfterEach
    fun tearDown() {
        Scout.Interceptors.clear()
    }

    @Test
    fun `Replacer created from builder replaces factories`() {
        Scout.Interceptors.register(
            ObjectFactoryReplacer.Builder()
                .factory<String> { "Replaced string" }
                .factory<Int> { 43 }
                .build()
        )
        val scope = scope("") {
            factory<String> { "Original string" }
            factory<Int> { 42 }
        }
        val component = object : Component(scope) {
            fun string(): String = get()
            fun number(): Int = get()
        }
        assertThat(component.string()).isEqualTo("Replaced string")
        assertThat(component.number()).isEqualTo(43)
    }

    @Test
    fun `Replacer created from lambda replaces factories`() {
        Scout.Interceptors.register(
            ObjectFactoryReplacer {
                factory<String> { "Replaced string" }
                factory<Int> { 43 }
            }
        )
        val scope = scope("") {
            factory<String> { "Original string" }
            factory<Int> { 42 }
        }
        val component = object : Component(scope) {
            fun string(): String = get()
            fun number(): Int = get()
        }
        assertThat(component.string()).isEqualTo("Replaced string")
        assertThat(component.number()).isEqualTo(43)
    }

    @Test
    fun `Replacer factory works like factory`() {
        class SomeType
        Scout.Interceptors.register(
            ObjectFactoryReplacer {
                factory<SomeType> { SomeType() }
            }
        )
        val scope = scope("") {
            singleton<SomeType> { SomeType() }
        }
        val component = object : Component(scope) {
            fun some(): SomeType = get()
        }
        assertThat(component.some()).isNotEqualTo(component.some())
    }

    @Test
    fun `Replacer singleton works like factory`() {
        class SomeType
        Scout.Interceptors.register(
            ObjectFactoryReplacer {
                singleton<SomeType> { SomeType() }
            }
        )
        val scope = scope("") {
            factory<SomeType> { SomeType() }
        }
        val component = object : Component(scope) {
            fun some(): SomeType = get()
        }
        assertThat(component.some()).isEqualTo(component.some())
    }

    @Test
    fun `Replacer replaces factories for lazy calls`() {
        Scout.Interceptors.register(
            ObjectFactoryReplacer {
                factory<String> { "replaced-string" }
            }
        )
        val scope = scope("") {
            factory<String> { "original-string" }
        }
        val component = object : Component(scope) {
            fun string(): Lazy<String> = getLazy()
        }
        assertThat(component.string().value).isEqualTo("replaced-string")
    }

    @Test
    fun `Replacer replaces factories for provider calls`() {
        Scout.Interceptors.register(
            ObjectFactoryReplacer {
                factory<String> { "replaced-string" }
            }
        )
        val scope = scope("") {
            factory<String> { "original-string" }
        }
        val component = object : Component(scope) {
            fun string(): Provider<String> = getProvider()
        }
        assertThat(component.string().get()).isEqualTo("replaced-string")
    }
}
