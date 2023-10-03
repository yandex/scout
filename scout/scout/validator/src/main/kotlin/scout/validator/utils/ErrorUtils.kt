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
package scout.validator.utils

import scout.error.DefinitionNullabilityError
import scout.error.FailedElementCreationError
import scout.error.FailedMappingCreationError
import scout.error.FailedObjectCreationError
import scout.error.IllegalDefinitionOverridesError
import scout.error.MissingAssociationMappingsError
import scout.error.MissingCollectionElementsError
import scout.error.MissingObjectFactoryError
import scout.error.ScopeInitializationError
import scout.error.ScoutError
import scout.error.UncheckedScoutError
import scout.error.WrongAccessorMethodError

internal fun findRootCause(error: ScoutError): ScoutError {
    return when (error) {
        is ScopeInitializationError,
        is MissingObjectFactoryError,
        is MissingCollectionElementsError,
        is MissingAssociationMappingsError,
        is IllegalDefinitionOverridesError,
        is DefinitionNullabilityError,
        is WrongAccessorMethodError,
        is UncheckedScoutError -> error
        is FailedObjectCreationError -> findRootCause(error.cause)
        is FailedElementCreationError -> findRootCause(error.cause)
        is FailedMappingCreationError -> findRootCause(error.cause)
    }
}
