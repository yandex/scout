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
package scout.validator

import scout.Component
import scout.validator.checker.ConsistencyChecker
import scout.validator.checker.ScopeNamingChecker
import scout.validator.checker.OverridesChecker
import scout.validator.model.ValidationResult
import scout.validator.tools.component.ComponentProducer
import scout.validator.tools.method.MethodFilter
import scout.validator.tools.method.MethodInvoker
import scout.validator.utils.doubledLineSeparator
import scout.validator.utils.lineSeparator

/**
 * Allows to validate [Component]s by specified [Checker]s.
 *
 * Use builtin checkers from [scout.validator.checker] package
 * or implement custom [Checker]s for app-specific checks.
 *
 * Use configurator to set up validation in builder-pattern manner:
 * ```
 * Validator.configure()
 *     .withConsistencyCheck()
 *     .withRedundancyCheck()
 *     .withCustomCheck(MyChecker())
 *     .validate(ComponentProducer.of(components))
 * ```
 */
class Validator(
    private val producer: ComponentProducer,
    private val checkers: List<Checker>
) {

    /**
     * Validate components produced by [producer] using specified [Checker]s.
     * This method throws exception if validation fails.
     */
    fun validate() {
        val results = validateWithResults()
        val fails = results.filterIsInstance<ValidationResult.Failed>()
        if (fails.isNotEmpty()) {
            throw ValidationException(formatErrorMessage(fails))
        }
    }

    /**
     * Validate components produced by [producer] using specified [Checker]s.
     * This method returns validation result for further processing.
     */
    fun validateWithResults(): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()
        val components = producer.produce().toList()
        println(formatAnnounceMessage(checkers, components))
        for (checker in checkers) {
            val result = checker.check(components)
            when (result) {
                is ValidationResult.Passed -> println(formatSuccessMessage(result))
                is ValidationResult.Failed -> println(formatFailedMessage(result))
            }
            results += result
        }
        return results
    }

    private fun formatAnnounceMessage(checkers: List<Checker>, components: List<Component>): String {
        return checkers.joinToString(
            prefix = "Validate ${components.size} components using "
        ) { checker -> checker::class.simpleName!! }
    }

    private fun formatSuccessMessage(result: ValidationResult.Passed): String {
        return " ✔ ${result.message}"
    }

    private fun formatFailedMessage(result: ValidationResult.Failed): String {
        return " ✘ ${result.message}"
    }

    private fun formatErrorMessage(fails: List<ValidationResult.Failed>): String {
        return fails.joinToString(
            prefix = doubledLineSeparator,
            separator = doubledLineSeparator,
            postfix = "$lineSeparator––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––"
        ) { result ->
            formatFailDetails(result)
        }
    }

    /**
     * Converts [ValidationResult.Failed] to string in format:
     * [Some check failed]
     *
     *   ✘ [Some error message] (5 errors)
     *     Some error details
     *
     *   ✘ [Other error message] (3 errors)
     *     Other error details
     */
    private fun formatFailDetails(fail: ValidationResult.Failed): String {
        val clusters = fail.errors.groupBy { error -> error.id }
        return buildString {
            appendLine("[${fail.message}]")
            for ((_, cluster) in clusters) {
                val error = cluster.first()
                val message = error.message
                val details = error.exception?.stackTraceToString()
                val detailsWithIndent = details?.lineSequence()
                    ?.filter { line -> line.isNotEmpty() }
                    ?.joinToString(separator = lineSeparator) { line -> "\t$line" }
                appendLine()
                appendLine("  ✘ [${message}] (${cluster.size} errors)")
                if (detailsWithIndent != null) {
                    appendLine(detailsWithIndent)
                }
            }
        }
    }

    /**
     * Allows to configure [Validator] in builder-pattern manner. Use "with*" methods
     * to configure checks. Then call [validate] or [validateWithResults] for validation.
     */
    class Configurator {

        private val checkers = mutableListOf<Checker>()

        /**
         * Validate using [ConsistencyChecker] with specified [methodFilter] and [methodInvoker].
         */
        fun withConsistencyCheck(
            methodFilter: MethodFilter = MethodFilter.any,
            methodInvoker: MethodInvoker = MethodInvoker.default(null)
        ) = apply { checkers += ConsistencyChecker(methodFilter, methodInvoker) }

        /**
         * Validate using [OverridesChecker].
         */
        fun withOverridesCheck() = apply { checkers += OverridesChecker() }

        /**
         * Validate using [ScopeNamingChecker].
         */
        fun withScopeNamingChecker(
            scopeNameRule: (String) -> Boolean
        ) = apply { checkers += ScopeNamingChecker(scopeNameRule) }

        /**
         * Validate using specified [Checker].
         */
        fun withCustomCheck(
            checker: Checker
        ) = apply { checkers += checker }

        /**
         * Validate components produced by [producer] using specified [Checker]s.
         * This method throws exception if validation fails.
         */
        fun validate(producer: ComponentProducer) {
            Validator(producer, checkers).validate()
        }

        /**
         * Validate components produced by [producer] using specified [Checker]s.
         * This method returns validation result for further processing.
         */
        fun validateWithResults(producer: ComponentProducer): List<ValidationResult> {
            return Validator(producer, checkers).validateWithResults()
        }
    }

    companion object {

        /**
         * Create (empty) validation configurator.
         *
         * Usage example:
         * ```
         * Validator.configure()
         *     .withConsistencyCheck()
         *     .withRedundancyCheck()
         *     .withCustomCheck(MyChecker())
         *     .validate(ComponentProducer.of(components))
         * ```
         */
        fun configure() = Configurator()
    }
}

internal class ValidationException(report: String) : RuntimeException(report) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
