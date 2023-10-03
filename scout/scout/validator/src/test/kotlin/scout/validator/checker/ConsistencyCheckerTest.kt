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
import scout.validator.tools.method.MethodFilter
import scout.validator.tools.method.MethodInvoker

class ConsistencyCheckerTest {

    @Test
    fun `Test that checker passes for consistent scope`() {
        val result = checker()
            .check(listOf(ConsistentComponent()))
        assertThat(result).isInstanceOf(ValidationResult.Passed::class.java)
    }

    @Test
    fun `Test that checker fails for inconsistent scope`() {
        val result = checker()
            .check(listOf(InconsistentComponent()))
        assertThat(result).isInstanceOf(ValidationResult.Failed::class.java)
    }

    @Test
    fun `Test that checker passes with consistent defers`() {
        val result = checker()
            .check(listOf(ConsistentComponentWithDefers()))
        assertThat(result).isInstanceOf(ValidationResult.Passed::class.java)
    }

    @Test
    fun `Test that checker fails with inconsistent defers`() {
        val result = checker()
            .check(listOf(InconsistentComponentWithDefers()))
        assertThat(result).isInstanceOf(ValidationResult.Failed::class.java)
    }

    @Test
    fun `Test that checker fails with non-empty message and errors list`() {
        val result = checker()
            .check(listOf(InconsistentComponent()))
        require(result is ValidationResult.Failed)
        assertThat(result.message).isNotEmpty()
        assertThat(result.errors).isNotEmpty()
    }

    @Test
    fun `Test that checker fails for consistent and inconsistent scope`() {
        val result = checker()
            .check(listOf(ConsistentComponent(), InconsistentComponent()))
        assertThat(result).isInstanceOf(ValidationResult.Failed::class.java)
    }

    @Test
    fun `Test that checker accumulates errors`() {
        val result = checker()
            .check(listOf(InconsistentComponent()))
        require(result is ValidationResult.Failed)
        assertThat(result.message).isEqualTo("Consistency check failed with 2 errors")
        assertThat(result.errors).hasSize(2)
    }

    @Test
    fun `Test that checker errors identifies by root cause`() {
        val result = checker()
            .check(listOf(InconsistentComponent()))
        require(result is ValidationResult.Failed)
        val ids = result.errors.map { error -> error.id }
        assertThat(ids.distinct().size).isEqualTo(1)
    }

    @Test
    fun `Test that checker errors contains failed method references`() {
        val result = checker()
            .check(listOf(InconsistentComponent()))
        require(result is ValidationResult.Failed)
        val messages = result.errors.map { error -> error.message }
        assertThat(messages)
            .anyMatch { message -> message.contains("getString") }
            .anyMatch { message -> message.contains("getNumber") }
    }

    @Test
    fun `Test that checker applies specified filter`() {
        val result = checkerWithFilter { false }
            .check(listOf(InconsistentComponent()))
        assertThat(result).isInstanceOf(ValidationResult.Passed::class.java)
    }

    @Test
    fun `Test that checker uses specified invoker`() {
        var called = false
        val invoker = MethodInvoker.default { _, _, _ ->
            called = true
            return@default 0
        }
        val result = checkerWithInvoker(invoker)
            .check(listOf(ComponentWithAssistance()))
        assertThat(called).isEqualTo(true)
        assertThat(result).isInstanceOf(ValidationResult.Passed::class.java)
    }

    private fun checker() = ConsistencyChecker(
        methodFilter = MethodFilter.any,
        methodInvoker = MethodInvoker.default(null)
    )

    private fun checkerWithFilter(filter: MethodFilter) = ConsistencyChecker(
        methodFilter = filter,
        methodInvoker = MethodInvoker.default(null)
    )

    private fun checkerWithInvoker(invoker: MethodInvoker) = ConsistencyChecker(
        methodFilter = MethodFilter.any,
        methodInvoker = invoker
    )
}

private val consistentScope = scope("consistent-scope") {
    factory<Int> { 42 }
    factory<String> { get<Int>().toString() }
}

private val inconsistentScope = scope("inconsistent-scope") {
    factory<String> { get<Int>().toString() }
}

private val consistentScopeWithDefers = scope("consistent-scope-with-defers") {
    factory<Int> { 42 }
    factory<String> { getLazy<Int>(); "" }
}

private val inconsistentScopeWithDefers = scope("inconsistent-scope-with-defers") {
    factory<String> { getLazy<Int>(); "" }
}

private class ConsistentComponent : Component(consistentScope) {

    @Suppress("unused")
    fun getString() = get<String>()
}

private class InconsistentComponent : Component(inconsistentScope) {

    @Suppress("unused")
    fun getString() = get<String>()

    @Suppress("unused")
    fun getNumber() = get<Int>()
}

private class ComponentWithAssistance : Component(consistentScope) {

    @Suppress("unused")
    fun repeatedString(repeats: Int) = get<String>().repeat(repeats)
}

private class ConsistentComponentWithDefers : Component(consistentScopeWithDefers) {

    @Suppress("unused")
    fun getString() = get<String>()
}

private class InconsistentComponentWithDefers : Component(inconsistentScopeWithDefers) {

    @Suppress("unused")
    fun getString() = get<String>()
}
