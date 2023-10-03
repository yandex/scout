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
package scout.error

import scout.builtin.BuiltinKeys
import scout.definition.AssociationKeys
import scout.definition.CollectionKeys
import scout.definition.ObjectKeys
import scout.exception.ElementCreationFailedException
import scout.exception.IllegalOverridesException
import scout.exception.MappingCreationFailedException
import scout.exception.MissingCollectionElementsException
import scout.exception.MissingMappingException
import scout.exception.MissingObjectFactoryException
import scout.exception.ObjectCreationFailedException
import scout.exception.ObjectNullabilityException
import scout.exception.ScopeInitializationException
import scout.exception.ScoutException

/**
 * This class is responsible for converting [Throwable] to [ScoutError]
 * ([UncheckedScoutError] if throwable is unexpected).
 */
object ScoutErrorFormatter {

    /**
     * Formats [ScoutError] from [throwable].
     */
    fun format(throwable: Throwable): ScoutError {
        return if (throwable is ScoutException) {
            when (throwable) {
                is ScopeInitializationException -> ScopeInitializationError(
                    name = throwable.name,
                    message = "Failed initialization of scope \"${throwable.name}\"",
                    exception = throwable,
                )
                is MissingObjectFactoryException -> when (throwable.key) {
                    BuiltinKeys.OBJECT_KEY_LAZY -> WrongAccessorMethodError(
                        key = throwable.key,
                        message = "Wrong access method for \"Lazy\" instance",
                        exception = throwable,
                    )
                    BuiltinKeys.OBJECT_KEY_PROVIDER -> WrongAccessorMethodError(
                        key = throwable.key,
                        message = "Wrong access method for \"Provider\" instance",
                        exception = throwable,
                    )
                    BuiltinKeys.OBJECT_KEY_UNIT -> WrongAccessorMethodError(
                        key = throwable.key,
                        message = "Wrong access method for \"Unit\" instance",
                        exception = throwable
                    )
                    else -> MissingObjectFactoryError(
                        key = throwable.key,
                        message = "Missing factory for ${ObjectKeys.format(throwable.key)}",
                        exception = throwable,
                    )
                }
                is MissingCollectionElementsException -> MissingCollectionElementsError(
                    key = throwable.key,
                    message = "Missing elements for ${CollectionKeys.format(throwable.key)}",
                    exception = throwable,
                )
                is MissingMappingException -> MissingAssociationMappingsError(
                    key = throwable.key,
                    message = "Missing mappings for ${AssociationKeys.format(throwable.key)}",
                    exception = throwable,
                )
                is IllegalOverridesException -> IllegalDefinitionOverridesError(
                    keys = throwable.keys,
                    message = "Illegal override of ${throwable.keys.map { key -> ObjectKeys.format(key) }}",
                    exception = throwable,
                )
                is ObjectNullabilityException -> DefinitionNullabilityError(
                    key = throwable.key,
                    message = "Nullability error of ${ObjectKeys.format(throwable.key)}",
                    exception = throwable,
                )
                is ObjectCreationFailedException -> FailedObjectCreationError(
                    key = throwable.key,
                    cause = format(throwable.cause),
                    message = "Failed creation of ${ObjectKeys.format(throwable.key)}",
                    exception = throwable,
                )
                is ElementCreationFailedException -> FailedElementCreationError(
                    key = throwable.key,
                    cause = format(throwable.cause),
                    message = "Failed creation of ${CollectionKeys.format(throwable.key)}",
                    exception = throwable,
                )
                is MappingCreationFailedException -> FailedMappingCreationError(
                    key = throwable.key,
                    cause = format(throwable.cause),
                    message = "Failed creation of ${AssociationKeys.format(throwable.key)}",
                    exception = throwable,
                )
            }
        } else {
            UncheckedScoutError(
                message = "Unchecked error with message: ${throwable.message}",
                exception = throwable,
            )
        }
    }
}
