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

import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey

/**
 * Base class for all known errors.
 */
sealed interface ScoutError {

    /**
     * Identifies error for further analysing.
     */
    val id: String

    /**
     * Message with error details.
     */
    val message: String

    /**
     * Exception that caused the problem.
     */
    val exception: Throwable?
}

/**
 * Scope initialization failed with [message] and [exception].
 */
class ScopeInitializationError(
    val name: String,
    override val message: String,
    override val exception: Throwable
) : ScoutError {

    override val id = "ScopeInitializationError#scope($name)"
}

/**
 * Object factory is missing.
 */
class MissingObjectFactoryError(
    val key: ObjectKey,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "MissingObjectFactoryError#type($key)"
}

/**
 * Collection elements is missing.
 */
class MissingCollectionElementsError(
    val key: CollectionKey,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "MissingCollectionElementsError#items($key)"
}

/**
 * Association mappings is missing.
 */
class MissingAssociationMappingsError(
    val key: AssociationKey,
    override val message: String,
    override val exception: Throwable?,
) : ScoutError {

    override val id = "MissingAssociationMappingsError#mappings(${key})"
}

/**
 * Factory override is forbidden.
 */
class IllegalDefinitionOverridesError(
    val keys: Set<ObjectKey>,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "IllegalDefinitionOverrideError#type($keys)"
}

/**
 * Required non-null instance but null got.
 */
class DefinitionNullabilityError(
    val key: ObjectKey,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "DefinitionNullabilityError#type($key)"
}

/**
 * Requested forbidden type.
 */
class WrongAccessorMethodError(
    val key: ObjectKey,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "WrongAccessorMethodError#stacktrace(${exception?.stackTraceToString()})"
}

/**
 * Object instance creation failed.
 */
class FailedObjectCreationError(
    val key: ObjectKey,
    val cause: ScoutError,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "FailedObjectCreationError#type($key)"
}

/**
 * Element creation failed.
 */
class FailedElementCreationError(
    val key: CollectionKey,
    val cause: ScoutError,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "FailedElementCreationError#type($key)"
}

/**
 * Mapping creation failed.
 */
class FailedMappingCreationError(
    val key: AssociationKey,
    val cause: ScoutError,
    override val message: String,
    override val exception: Throwable?
) : ScoutError {

    override val id = "FailedMappingCreationError#mappings(${key})"
}

/**
 * Unknown problem.
 */
class UncheckedScoutError(
    override val message: String,
    override val exception: Throwable
) : ScoutError {

    override val id = "UncheckedScoutError#message($message)"
}
