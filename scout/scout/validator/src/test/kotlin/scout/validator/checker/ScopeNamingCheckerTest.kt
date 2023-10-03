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
package scout.validator.checker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import scout.Component
import scout.scope
import scout.validator.model.ValidationResult

class ScopeNamingCheckerTest {

    @Test
    fun `Test that checker passes for scope with correct name`() {
        val scope = scope("some-name") {}
        val component = object : Component(scope) {}
        val result = checker { name -> name.isNotEmpty() }
            .check(listOf(component))
        assertThat(result).isInstanceOf(ValidationResult.Passed::class.java)
    }

    @Test
    fun `Test that checker fails for scope with incorrect name`() {
        val scope = scope("") {}
        val component = object : Component(scope) {}
        val result = checker { name -> name.isNotEmpty() }
            .check(listOf(component))
        assertThat(result).isInstanceOf(ValidationResult.Failed::class.java)
    }

    @Test
    fun `Test that checker fails with non-empty message and errors list`() {
        val scope = scope("some-scope") {}
        val component = object : Component(scope) {}
        val result = checker { false }
            .check(listOf(component))
        require(result is ValidationResult.Failed)
        assertThat(result.message).isNotEmpty()
        assertThat(result.errors).isNotEmpty()
    }

    @Test
    fun `Test that checker fails if at least one scope has incorrect name`() {
        val scope1 = scope("  \n ") {}
        val component1 = object : Component(scope1) {}
        val scope2 = scope("  \n f") {}
        val component2 = object : Component(scope2) {}
        val result = checker { name -> name.isNotBlank() }
            .check(listOf(component1, component2))
        assertThat(result).isInstanceOf(ValidationResult.Failed::class.java)
    }

    @Test
    fun `Test that checker accumulates errors`() {
        val scope1 = scope("  \n ") {}
        val component1 = object : Component(scope1) {}
        val scope2 = scope("") {}
        val component2 = object : Component(scope2) {}
        val result = checker { name -> name.isNotBlank() }
            .check(listOf(component1, component2))
        require(result is ValidationResult.Failed)
        assertThat(result.message).isEqualTo("Scope naming check failed with 2 errors")
        assertThat(result.errors).hasSize(2)
    }

    @Test
    fun `Test that checker errors identifies by scope names`() {
        val scope1 = scope("foo") {}
        val component1 = object : Component(scope1) {}
        val scope2 = scope("bar") {}
        val component2 = object : Component(scope2) {}
        val scope3 = scope("bar") {}
        val component3 = object : Component(scope3) {}
        val result = checker { false }
            .check(listOf(component1, component2, component3))
        require(result is ValidationResult.Failed)
        val ids = result.errors.map { error -> error.id }
        assertThat(ids.distinct().size).isEqualTo(2)
    }

    @Test
    fun `Test that checker errors contains problem scope names`() {
        val scope1 = scope("foo") {}
        val component1 = object : Component(scope1) {}
        val scope2 = scope("bar") {}
        val component2 = object : Component(scope2) {}
        val result = checker { false }
            .check(listOf(component1, component2))
        require(result is ValidationResult.Failed)
        val messages = result.errors.map { error -> error.message }
        assertThat(messages)
            .anyMatch { message -> message.contains("foo") }
            .anyMatch { message -> message.contains("bar") }
    }

    private fun checker(scopeNamingRule: (String) -> Boolean) = ScopeNamingChecker(scopeNamingRule)
}
