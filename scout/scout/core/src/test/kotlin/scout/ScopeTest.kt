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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import scout.exception.ScopeInitializationException
import scout.scope.access.DirectAccessor

class ScopeTest {

    @Test
    fun `Scope provides types declared in modules`() {
        val scope = scope("test-scope") {
            factory<String> { "Hello, world!" }
        }
        val accessor = DirectAccessor(scope)
        assertThat(accessor.get<String>()).isEqualTo("Hello, world!")
    }

    @Test
    fun `Scope throws initialization exception if type declared in several modules`() {
        assertThrows<ScopeInitializationException> {
            scope("test-scope") {
                factory<String> { "Hello," }
                factory<String> { "world!" }
            }
        }
    }

    @Test
    fun `Scope provides types declared in parent scope`() {
        val parentScope = scope("parent-test-scope") {
            factory<String> { "Hello, world!" }
        }
        val scope = scope("test-scope") {
            dependsOn(parentScope)
        }
        val accessor = DirectAccessor(scope)
        assertThat(accessor.get<String>()).isEqualTo("Hello, world!")
    }

    @Test
    fun `Scope overrides types declared in parent scope`() {
        val parentScope = scope("parent-test-scope") {
            factory<String> { "Hello, world!" }
        }
        val scope = scope("test-scope") {
            dependsOn(parentScope)
            factory<String>(allowOverride = true) { "Hello, horde!" }
        }
        val accessor = DirectAccessor(scope)
        assertThat(accessor.get<String>()).isEqualTo("Hello, horde!")
    }

    @Test
    fun `Later scope have bigger priority`() {
        val coreScope = scope("core-scope") {
            factory<Boolean> { false }
        }
        val settingsScope = scope("settings-scope") {
            dependsOn(coreScope)
            factory<Boolean>(allowOverride = true) { true }
        }
        val featureScope = scope("feature-scope") {
            dependsOn(coreScope)
            dependsOn(settingsScope)
        }

        class FeatureComponent : Component(featureScope) {
            fun getFlag() = get<Boolean>()
        }

        assertThat(FeatureComponent().getFlag()).isEqualTo(true)
    }
}
