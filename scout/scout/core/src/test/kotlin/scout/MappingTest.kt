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
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import scout.exception.MissingMappingException
import scout.scope.access.DirectAccessor

class MappingTest {

    @Test
    fun `Association contains all declared bindings`() {
        val firstKey = "first-key"
        val firstValue = "first-value"
        val secondKey = "second-key"
        val secondValue = "second-value"
        val scope = scope("test-scope") {
            mapping<String, String> {
                firstKey to firstValue
            }
            mapping<String, String> {
                secondKey to secondValue
            }
        }
        val accessor = DirectAccessor(scope)
        val association = accessor.associate<String, String>()
        assertThat(association)
            .contains(entry(firstKey, firstValue), entry(secondKey, secondValue))
    }

    @Test
    fun `Throws an exception if requested non-empty but bindings are not declared`() {
        val scope = scope("test-scope") {}
        val accessor = DirectAccessor(scope)
        assertThrows<MissingMappingException> {
            accessor.associate<Any, Any>(nonEmpty = true)
        }
    }

    @Test
    fun `Returns empty if mapping are not declared and empty allowed`() {
        val scope = scope("test-scope") {}
        val accessor = DirectAccessor(scope)
        val mapping = accessor.associate<Any, Any>()
        assertThat(mapping).isEmpty()
    }

    @Test
    fun `Bindings from dependencies exists in association`() {
        val coreScope = scope("core-scope") {
            mapping<String, String> {
                "foo" to "foo"
            }
        }
        val settingsScope = scope("settings-scope") {
            dependsOn(coreScope)
            mapping<String, String> {
                "bar" to "bar"
            }
        }
        val featureScope = scope("feature-scope") {
            dependsOn(coreScope)
            dependsOn(settingsScope)
        }

        val accessor = DirectAccessor(featureScope)
        val association = accessor.associate<String, String>()
        assertThat(association)
            .contains(entry("foo", "foo"), entry("bar", "bar"))
    }
}
