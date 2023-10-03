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
import scout.exception.MissingCollectionElementsException
import scout.scope.access.DirectAccessor

class AccessorCollectUsageTest {

    @Test
    fun `accessor#collect() returns non-nullable definition`() {
        val expected = "Hello, world!"
        val scope = scopeWithStringItemDefinition(expected)
        assertThat(DirectAccessor(scope).collect<String>()).isEqualTo(listOf(expected))
    }

    @Test
    fun `accessor#collectionProvider() returns non-nullable definition provider`() {
        val expected = "Hello, world!"
        val scope = scopeWithStringItemDefinition(expected)
        assertThat(DirectAccessor(scope).collectProvider<String>().get()).isEqualTo(listOf(expected))
    }

    @Test
    fun `accessor#collection() returns non-nullable definition lazy`() {
        val expected = "Hello, world!"
        val scope = scopeWithStringItemDefinition(expected)
        assertThat(DirectAccessor(scope).collectLazy<String>().value).isEqualTo(listOf(expected))
    }

    @Test
    fun `accessor#collectOrEmpty() returns non-nullable definition`() {
        val expected = "Hello, world!"
        val scope = scopeWithStringItemDefinition(expected)
        assertThat(DirectAccessor(scope).collect<String>()).isEqualTo(listOf(expected))
    }

    @Test
    fun `accessor#collection() throws error if requested non-empty and items not defined`() {
        val scope = scopeWithoutDefinitions()
        assertThrows<MissingCollectionElementsException> {
            DirectAccessor(scope).collect<String>(nonEmpty = true)
        }
    }

    @Test
    fun `accessor#collection() throws error if requested non-empty and items for provider not defined`() {
        val scope = scopeWithoutDefinitions()
        assertThrows<MissingCollectionElementsException> {
            DirectAccessor(scope).collectProvider<String>(nonEmpty = true).get()
        }
    }

    @Test
    fun `accessor#collection() throws error if requested non-empty and items for lazy not defined`() {
        val scope = scopeWithoutDefinitions()
        assertThrows<MissingCollectionElementsException> {
            DirectAccessor(scope).collectLazy<String>(nonEmpty = true).value
        }
    }

    @Test
    fun `accessor#collectOrEmpty() returns empty if items not defined`() {
        val scope = scopeWithoutDefinitions()
        assertThat(DirectAccessor(scope).collect<String>()).isEqualTo(emptyList<String>())
    }

    private fun scopeWithoutDefinitions(): Scope {
        return scope("test-scope") {
            // no definitions
        }
    }

    private fun scopeWithStringItemDefinition(string: String): Scope {
        return scope("test-scope") {
            element<String> { string }
        }
    }
}
