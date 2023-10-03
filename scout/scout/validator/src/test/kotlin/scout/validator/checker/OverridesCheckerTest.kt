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

class OverridesCheckerTest {

    @Test
    fun `Test that checker passes for scope without overrides`() {
        val result = checker()
            .check(listOf(ComponentWithoutOverrides()))
        assertThat(result).isInstanceOf(ValidationResult.Passed::class.java)
    }

    @Test
    fun `Test that checker passes for scope with legal overrides`() {
        val result = checker()
            .check(listOf(ComponentWithLegalOverrides()))
        assertThat(result).isInstanceOf(ValidationResult.Passed::class.java)
    }

    @Test
    fun `Test that checker fails for scope with illegal overrides`() {
        val result = checker()
            .check(listOf(ComponentWithIllegalOverrides()))
        assertThat(result).isInstanceOf(ValidationResult.Failed::class.java)
    }

    @Test
    fun `Test that checker fails with non-empty message and errors list`() {
        val result = checker()
            .check(listOf(ComponentWithIllegalOverrides()))
        require(result is ValidationResult.Failed)
        assertThat(result.message).isNotEmpty()
        assertThat(result.errors).isNotEmpty()
    }

    @Test
    fun `Test that checker fails if at least one scope has illegal override`() {
        val result = checker()
            .check(listOf(ComponentWithLegalOverrides(), ComponentWithIllegalOverrides()))
        assertThat(result).isInstanceOf(ValidationResult.Failed::class.java)
    }

    @Test
    fun `Test that checker accumulates errors`() {
        val result = checker()
            .check(listOf(ComponentWithIllegalOverrides()))
        require(result is ValidationResult.Failed)
        assertThat(result.message).isEqualTo("Overrides check failed with 2 errors")
        assertThat(result.errors).hasSize(2)
    }

    @Test
    fun `Test that checker errors identifies by object key`() {
        val result = checker()
            .check(listOf(ComponentWithIllegalOverrides()))
        require(result is ValidationResult.Failed)
        val ids = result.errors.map { error -> error.id }
        assertThat(ids.distinct().size).isEqualTo(2)
    }

    @Test
    fun `Test that checker errors contains problem scope references`() {
        val result = checker()
            .check(listOf(ComponentWithIllegalOverrides()))
        require(result is ValidationResult.Failed)
        val messages = result.errors.map { error -> error.message }
        assertThat(messages)
            .allMatch { message -> message.contains("base-scope") }
            .allMatch { message -> message.contains("feature-scope") }
    }

    private fun checker() = OverridesChecker()
}

private val baseScope = scope("base-scope") {
    factory<String> { "base-string" }
    factory<Int> { 42 }
}

private val scopeWithoutOverrides = scope("feature-scope") {
    dependsOn(baseScope)
}

private val scopeWithLegalOverrides = scope("feature-scope") {
    dependsOn(baseScope)
    factory<String>(allowOverride = true) { "feature-string" }
}

private val scopeWithIllegalOverrides = scope("feature-scope") {
    dependsOn(baseScope)
    factory<String> { "feature-string" }
    factory<Int> { 43 }
}

class ComponentWithoutOverrides : Component(scopeWithoutOverrides)
class ComponentWithLegalOverrides : Component(scopeWithLegalOverrides)
class ComponentWithIllegalOverrides : Component(scopeWithIllegalOverrides)
