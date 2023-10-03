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
import scout.definition.ObjectKey
import scout.definition.ObjectKeys
import scout.validator.Checker
import scout.validator.model.ValidationError
import scout.validator.model.ValidationResult
import scout.inspector.ScopeInspector
import scout.inspector.inspect

/**
 * Checks that scope of each component does not contain
 * illegal object factory overrides. In terms of this
 * class, "illegal override" means parent scope has object
 * factory for type and child scope has factory for this type
 * without "allowOverride" flag raise.
 */
class OverridesChecker : Checker {

    override fun check(components: List<Component>): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        for (component in components) {
            errors += checkScopeTree(component.scope)
        }
        return formatResult(errors)
    }

    private fun checkScopeTree(scope: Scope): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val inspector = scope.inspect()
        for (objectKey in inspector.nonOverridableDefinitions) {
            for (parent in scope.parents) {
                val provider = parent.findObjectProvider(objectKey)
                if (provider != null) {
                    errors += ValidationError(
                        id = "IllegalObjectOverride/${objectKey}",
                        message = "Scope \"${scope.name}\" overrides " +
                            "${ObjectKeys.format(objectKey)} " +
                            "already declared in parent scope \"${provider.name}\", " +
                            "but flag \"allowOverride\" is not raised"
                    )
                }
            }
        }
        for (parent in scope.parents) {
            errors += checkScopeTree(parent)
        }
        return errors
    }

    private val ScopeInspector.nonOverridableDefinitions: List<ObjectKey>
        get() = objectFactories.keys.filter { key ->
            key !in allowedObjectOverrides
        }

    private fun Scope.findObjectProvider(key: ObjectKey): Scope? {
        if (key in this.inspect().objectFactories) {
            return this
        }
        for (parent in this.parents) {
            val provider = parent.findObjectProvider(key)
            if (provider != null) {
                return provider
            }
        }
        return null
    }

    private fun formatResult(errors: List<ValidationError>): ValidationResult {
        if (errors.isNotEmpty()) {
            return ValidationResult.Failed(
                message = "Overrides check failed with ${errors.size} errors",
                errors = errors
            )
        }
        return ValidationResult.Passed("Overrides check passed without errors")
    }
}
