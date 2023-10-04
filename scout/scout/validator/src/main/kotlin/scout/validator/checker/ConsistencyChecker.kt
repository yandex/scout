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
import scout.Provider
import scout.Scout
import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey
import scout.error.ScoutErrorFormatter
import scout.scope.access.Accessor
import scout.scope.access.Interceptor
import scout.validator.Checker
import scout.validator.model.ValidationError
import scout.validator.model.ValidationResult
import scout.validator.tools.method.MethodFilter
import scout.validator.tools.method.MethodInvoker
import scout.validator.utils.findRootCause
import scout.validator.utils.formatMethodRef
import java.lang.reflect.Method

/**
 * Checks that each component method (corresponding to the [methodFilter])
 * completes without errors and deferred requests (lazies and providers)
 * completes without errors too. Methods invokes using [methodInvoker] so
 * you can customize invoke logic (for example, argument stubbing).
 */
class ConsistencyChecker(
    private val methodFilter: MethodFilter,
    private val methodInvoker: MethodInvoker
) : Checker {

    override fun check(components: List<Component>): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        for (component in components) {
            for (method in component::class.java.declaredMethods) {
                if (methodFilter.matches(method)) {
                    errors += checkComponentMethod(component, method)
                }
            }
        }
        return formatResult(errors)
    }

    private fun checkComponentMethod(
        component: Component,
        method: Method
    ): List<ValidationError> {
        val lazies = mutableListOf<Lazy<*>>()
        val providers = mutableListOf<Provider<*>>()
        val lazyDetector = createLazyDetector { lazy -> lazies += lazy }
        val providerDetector = createProviderDetector { provider -> providers += provider }
        Scout.Interceptors.register(lazyDetector)
        Scout.Interceptors.register(providerDetector)
        try {
            methodInvoker.invoke(component, method)
        } catch (exception: Throwable) {
            Scout.Interceptors.unregister(lazyDetector)
            Scout.Interceptors.unregister(providerDetector)
            val error = ScoutErrorFormatter.format(exception)
            val methodRef = formatMethodRef(component::class.java, method)
            return listOf(
                ValidationError(
                    id = "ComponentMethodFailed/${findRootCause(error).id}",
                    message = "An error occurred while calling of method $methodRef",
                    exception = exception
                )
            )
        }
        return try {
            completeDeferredResolves(component, method, lazies, providers)
        } finally {
            Scout.Interceptors.unregister(lazyDetector)
            Scout.Interceptors.unregister(providerDetector)
        }
    }

    private fun completeDeferredResolves(
        component: Component,
        method: Method,
        lazies: MutableList<Lazy<*>>,
        providers: MutableList<Provider<*>>
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        var round = 1
        while (lazies.isNotEmpty() || providers.isNotEmpty()) {
            if (round > DEFERRED_RECURSION_THRESHOLD) {
                val methodRef = formatMethodRef(component::class.java, method)
                errors += ValidationError(
                    id = "DeferredOverflow/$methodRef",
                    message = "Recursive deferred threshold reached for $methodRef " +
                        "(it seems you have circular dependency)"
                )
                break
            }
            val roundLazies = lazies.toList(); lazies.clear()
            val roundProviders = providers.toList(); providers.clear()
            for (lazy in roundLazies) {
                try {
                    lazy.value
                } catch (exception: Throwable) {
                    val error = ScoutErrorFormatter.format(exception)
                    val methodRef = formatMethodRef(component::class.java, method)
                    errors += ValidationError(
                        id = "LazyResolveFailed/${findRootCause(error).id}",
                        message = "An error occurred while resolving lazies for $methodRef",
                        exception = exception
                    )
                }
            }
            for (provider in roundProviders) {
                try {
                    provider.get()
                } catch (exception: Throwable) {
                    val error = ScoutErrorFormatter.format(exception)
                    val methodRef = formatMethodRef(component::class.java, method)
                    errors += ValidationError(
                        id = "ProviderResolveFailed/${findRootCause(error).id}",
                        message = "An error occurred while resolving providers for $methodRef",
                        exception = exception
                    )
                }
            }
            round += 1
        }
        return errors
    }

    private fun formatResult(errors: List<ValidationError>): ValidationResult {
        if (errors.isNotEmpty()) {
            return ValidationResult.Failed(
                message = "Consistency check failed with ${errors.size} errors",
                errors = errors
            )
        }
        return ValidationResult.Passed("Consistency check passed without errors")
    }

    companion object {
        private const val DEFERRED_RECURSION_THRESHOLD = 99
    }
}

private fun createLazyDetector(onDetect: (Lazy<*>) -> Unit) = object : Interceptor.AfterLazy {
    override fun getLazy(key: ObjectKey, accessor: Accessor, result: Lazy<Any>) = result.apply(onDetect)
    override fun optLazy(key: ObjectKey, accessor: Accessor, result: Lazy<Any?>) = result.apply(onDetect)
    override fun collectLazy(key: CollectionKey, accessor: Accessor, result: Lazy<List<Any>>) = result.apply(onDetect)
    override fun associateLazy(key: AssociationKey, accessor: Accessor, result: Lazy<Map<*, *>>) = result.apply(onDetect)
}

private fun createProviderDetector(onDetect: (Provider<*>) -> Unit) = object : Interceptor.AfterProvider {
    override fun getProvider(key: ObjectKey, accessor: Accessor, result: Provider<Any>) = result.apply(onDetect)
    override fun optProvider(key: ObjectKey, accessor: Accessor, result: Provider<Any?>) = result.apply(onDetect)
    override fun collectProvider(key: CollectionKey, accessor: Accessor, result: Provider<List<Any>>) = result.apply(onDetect)
    override fun associateProvider(key: AssociationKey, accessor: Accessor, result: Provider<Map<*, *>>) = result.apply(onDetect)
}
