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
import scout.exception.MissingObjectFactoryException
import scout.exception.ObjectNullabilityException
import scout.scope.access.DirectAccessor

class AccessorGetUsageTest {

    @Test
    fun `accessor#get() returns non-nullable definition`() {
        val expected = "Hello, world!"
        val scope = scopeWithStringDefinition(expected)
        assertThat(DirectAccessor(scope).get<String>()).isEqualTo(expected)
        assertThat(DirectAccessor(scope).opt<String>()).isEqualTo(expected)
    }

    @Test
    fun `accessor#getProvider() returns non-nullable definition provider`() {
        val expected = "Hello, world!"
        val scope = scopeWithStringDefinition(expected)
        assertThat(DirectAccessor(scope).getProvider<String>().get()).isEqualTo(expected)
        assertThat(DirectAccessor(scope).optProvider<String>().get()).isEqualTo(expected)
    }

    @Test
    fun `accessor#getLazy() returns non-nullable definition lazy`() {
        val expected = "Hello, world!"
        val scope = scopeWithStringDefinition(expected)
        assertThat(DirectAccessor(scope).getLazy<String>().value).isEqualTo(expected)
        assertThat(DirectAccessor(scope).optLazy<String>().value).isEqualTo(expected)
    }

    @Test
    fun `accessor#get() returns nullable definition`() {
        val expected = "Hello, world!"
        val scope = scopeWithNullableStringDefinition(expected)
        assertThat(DirectAccessor(scope).get<String>()).isEqualTo(expected)
        assertThat(DirectAccessor(scope).opt<String>()).isEqualTo(expected)
    }

    @Test
    fun `accessor#getProvider() returns nullable definition provider`() {
        val expected = "Hello, world!"
        val scope = scopeWithNullableStringDefinition(expected)
        assertThat(DirectAccessor(scope).getProvider<String>().get()).isEqualTo(expected)
        assertThat(DirectAccessor(scope).optProvider<String>().get()).isEqualTo(expected)
    }

    @Test
    fun `accessor#getLazy() returns nullable definition lazy`() {
        val expected = "Hello, world!"
        val scope = scopeWithNullableStringDefinition(expected)
        assertThat(DirectAccessor(scope).getLazy<String>().value).isEqualTo(expected)
        assertThat(DirectAccessor(scope).optLazy<String>().value).isEqualTo(expected)
    }

    @Test
    fun `accessor#opt() returns null for nullable definition if nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).opt<String>()).isEqualTo(null)
    }

    @Test
    fun `accessor#optProvider() returns null provider for nullable definition if nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).optProvider<String>().get()).isEqualTo(null)
    }

    @Test
    fun `accessor#optLazy() returns null lazy for nullable definition if nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).optLazy<String>().value).isEqualTo(null)
    }

    @Test
    fun `accessor#getOrNull() returns null for nullable definition if nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).opt<String>()).isEqualTo(null)
    }

    @Test
    fun `accessor#getOrNull() returns null provider for nullable definition if nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).optProvider<String>().get()).isEqualTo(null)
    }

    @Test
    fun `accessor#getOrNull() returns null lazy for nullable definition if nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).optLazy<String>().value).isEqualTo(null)
    }

    @Test
    fun `accessor#get() throws error for nullable definition if non-nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThrows<ObjectNullabilityException> {
            DirectAccessor(scope).get<String>()
        }
    }

    @Test
    fun `accessor#getProvider() throws error for nullable definition if non-nullable provider requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThrows<ObjectNullabilityException> {
            DirectAccessor(scope).getProvider<String>().get()
        }
    }

    @Test
    fun `accessor#getLazy() throws error for nullable definition if non-nullable lazy requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThrows<ObjectNullabilityException> {
            DirectAccessor(scope).getLazy<String>().value
        }
    }

    @Test
    fun `accessor#getOrNull() returns null for nullable definition if non-nullable requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).opt<String>()).isEqualTo(null)
    }

    @Test
    fun `accessor#optProvider() returns null for nullable definition if non-nullable provider requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).optProvider<String>().get()).isEqualTo(null)
    }

    @Test
    fun `accessor#optLazy() returns null for nullable definition if non-nullable lazy requested`() {
        val scope = scopeWithNullableStringDefinition(null)
        assertThat(DirectAccessor(scope).optLazy<String>().value).isEqualTo(null)
    }

    @Test
    fun `accessor#get() throws error if definition not found`() {
        val scope = scopeWithoutDefinitions()
        assertThrows<MissingObjectFactoryException> {
            DirectAccessor(scope).get<String>()
        }
    }

    @Test
    fun `accessor#getProvider() throws error if definition for provider not found`() {
        val scope = scopeWithoutDefinitions()
        assertThrows<MissingObjectFactoryException> {
            DirectAccessor(scope).getProvider<String>().get()
        }
    }

    @Test
    fun `accessor#getLazy() throws error if definition for lazy not found`() {
        val scope = scopeWithoutDefinitions()
        assertThrows<MissingObjectFactoryException> {
            DirectAccessor(scope).getLazy<String>().value
        }
    }

    @Test
    fun `accessor#opt() returns null if definition not found`() {
        val scope = scopeWithoutDefinitions()
        assertThat(DirectAccessor(scope).opt<String>()).isEqualTo(null)
    }

    @Test
    fun `accessor#optProvider() returns provider with null if definition for provider not found`() {
        val scope = scopeWithoutDefinitions()
        assertThat(DirectAccessor(scope).optProvider<String>().get()).isEqualTo(null)
    }

    @Test
    fun `accessor#optLazy() returns lazy with null if definition for lazy not found`() {
        val scope = scopeWithoutDefinitions()
        assertThat(DirectAccessor(scope).optLazy<String>().value).isEqualTo(null)
    }

    private fun scopeWithoutDefinitions(): Scope {
        return scope("test-scope") {
            // no definitions
        }
    }

    private fun scopeWithStringDefinition(string: String): Scope {
        return scope("test-scope") {
            factory<String> { string }
        }
    }

    private fun scopeWithNullableStringDefinition(string: String?): Scope {
        return scope("test-scope") {
            factory<String?> { string }
        }
    }
}
