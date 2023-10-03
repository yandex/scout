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

class CollectionTest {

    interface BaseType

    @Test
    fun `Collection contains all declared items`() {
        val firstInstance = object : BaseType {}
        val secondInstance = object : BaseType {}
        val scope = scope("test-scope") {
            element<BaseType> {
                firstInstance
            }
            element<BaseType> {
                secondInstance
            }
        }
        val accessor = DirectAccessor(scope)
        val collection = accessor.collect<BaseType>()
        assertThat(collection).contains(firstInstance, secondInstance)
    }

    @Test
    fun `Instances of same final type can be collected`() {
        val scope = scope("test-scope") {
            element<String> { "foo" }
            element<String> { "bar" }
            element<String> { "baz" }
        }
        val accessor = DirectAccessor(scope)
        val collection = accessor.collect<String>()
        assertThat(collection).contains("foo", "bar", "baz")
    }

    @Test
    fun `Throw an exception if requested non-empty but items are not declared`() {
        val scope = scope("test-scope") {
            // no declarations
        }
        val accessor = DirectAccessor(scope)
        assertThrows<MissingCollectionElementsException> {
            accessor.collect<Any>(nonEmpty = true)
        }
    }

    @Test
    fun `Returns empty if items are not declared and empty allowed`() {
        val scope = scope("test-scope") {
            // no declarations
        }
        val accessor = DirectAccessor(scope)
        val collection = accessor.collect<Any>()
        assertThat(collection).isEmpty()
    }

    @Test
    fun `Repeating dependency don't lead to item duplication`() {
        val coreScope = scope("core-scope") {
            element<String> { "foo" }
        }
        val settingsScope = scope("settings-scope") {
            dependsOn(coreScope)
            element<String> { "bar" }
        }
        val featureScope = scope("feature-scope") {
            dependsOn(coreScope)
            dependsOn(settingsScope)
        }

        val accessor = DirectAccessor(featureScope)
        assertThat(accessor.collect<String>()).containsExactlyInAnyOrder("foo", "bar")
    }

    @Test
    fun `Order of items corresponds to order of dependencies`() {
        val fooScope = scope("foo-scope") {
            element<String> { "foo" }
        }
        val barScope = scope("bar-scope") {
            element<String> { "bar" }
        }
        val featureScope = scope("feature-scope") {
            dependsOn(fooScope)
            dependsOn(barScope)
        }

        val accessor = DirectAccessor(featureScope)
        assertThat(accessor.collect<String>()).containsExactly("foo", "bar")
    }

    @Test
    fun `Items of top scope are placed to the end of collection`() {
        val fooScope = scope("foo-scope") {
            element<String> { "foo" }
        }
        val featureScope = scope("feature-scope") {
            dependsOn(fooScope)
            element<String> { "bar" }
        }

        val accessor = DirectAccessor(featureScope)
        assertThat(accessor.collect<String>()).containsExactly("foo", "bar")
    }
}
