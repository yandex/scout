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

import scout.Component
import scout.Scope
import scout.validator.Checker
import scout.validator.model.ValidationError
import scout.validator.model.ValidationResult

/**
 * Check that scope name of each component satisfies the [rule].
 */
class ScopeNamingChecker(
    private val rule: (String) -> Boolean
) : Checker {

    override fun check(components: List<Component>): ValidationResult {
        val checked = mutableSetOf<Scope>()
        val errors = mutableListOf<ValidationError>()
        for (component in components) {
            if (checked.add(component.scope)) {
                errors += checkScopeName(component.scope)
            }
        }
        return formatResult(errors)
    }

    private fun checkScopeName(scope: Scope): List<ValidationError> {
        if (!rule(scope.name)) {
            return listOf(
                ValidationError(
                    id = "IllegalScopeName/${scope.name}",
                    message = "Scope has incorrect name \"${scope.name}\""
                )
            )
        }
        return emptyList()
    }

    private fun formatResult(errors: List<ValidationError>): ValidationResult {
        if (errors.isNotEmpty()) {
            return ValidationResult.Failed(
                message = "Scope naming check failed with ${errors.size} errors",
                errors = errors
            )
        }
        return ValidationResult.Passed("Scope naming check passed without errors")
    }
}
