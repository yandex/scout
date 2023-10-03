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
package scout.inspector

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import scout.definition.AssociationKeys
import scout.definition.CollectionKeys
import scout.definition.ObjectKeys
import scout.scope

class ScopeInspectorTest {

    @Test
    fun `Check empty scope inspection`() {
        val scope = scope("") { }
        val inspector = scope.inspect()
        assertThat(inspector.scope).isEqualTo(scope)
        assertThat(inspector.objectFactories).isEmpty()
        assertThat(inspector.collectionFactories).isEmpty()
        assertThat(inspector.associationFactories).isEmpty()
        assertThat(inspector.allowedObjectOverrides).isEmpty()
    }

    @Test
    fun `Check non-empty scope inspection`() {
        val scope = scope("") {
            factory<String> { "test-string" }
            singleton<Int> { 42 }
            element<String> { "foo" }
            element<String> { "bar" }
            element<String> { "baz" }
            mapping<String, String> { "ru" to "Привет" }
            mapping<String, String> { "en" to "Hello" }
        }
        val inspector = scope.inspect()
        assertThat(inspector.objectFactories).hasSize(2)
        assertThat(inspector.collectionFactories).hasSize(1)
        val collectionKey = CollectionKeys.create(String::class.java)
        assertThat(inspector.collectionFactories[collectionKey]).hasSize(3)
        assertThat(inspector.associationFactories).hasSize(1)
        val associationKey = AssociationKeys.create(String::class.java, String::class.java)
        assertThat(inspector.associationFactories[associationKey]).hasSize(2)
    }

    @Test
    fun `Check scope with overrides inspection`() {
        val scope = scope("") {
            factory<String>(allowOverride = true) { "test-string" }
        }
        val inspector = scope.inspect()
        assertThat(inspector.allowedObjectOverrides).containsExactly(ObjectKeys.create(String::class.java))
    }
}
