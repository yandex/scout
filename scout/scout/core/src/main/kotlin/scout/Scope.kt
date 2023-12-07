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
package scout

import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey
import scout.exception.ElementCreationFailedException
import scout.exception.MappingCreationFailedException
import scout.exception.MissingCollectionElementsException
import scout.exception.MissingMappingException
import scout.exception.MissingObjectFactoryException
import scout.exception.ObjectCreationFailedException
import scout.exception.ObjectNullabilityException
import scout.factory.InstanceFactory
import scout.map.ObjectFactoryMap
import scout.scope.access.Accessor
import scout.scope.access.DirectAccessor
import scout.scope.access.TrickyAccessor
import scout.scope.builder.ConfinedScopeBuilder
import scout.scope.builder.ScopeBuilder
import scout.scope.builder.SynchronizedScopeBuilder
import scout.scope.builder.UnsafeScopeBuilder
import scout.scope.utils.ScopeUtils
import scout.scope.utils.lrnParentLookup
import scout.scope.utils.nrlParentLookup

/**
 * Create [Scope] using specified [threadSafetyMode] and [configuration].
 */
inline fun scope(
    name: String,
    threadSafetyMode: ScopeBuilder.ThreadSafetyMode = Scout.ThreadSafety.scopeBuilderMode,
    configuration: ScopeBuilder.() -> Unit,
): Scope {
    return when (threadSafetyMode) {
        ScopeBuilder.ThreadSafetyMode.Unsafe -> UnsafeScopeBuilder(name)
        ScopeBuilder.ThreadSafetyMode.Confined -> ConfinedScopeBuilder(name)
        ScopeBuilder.ThreadSafetyMode.Synchronized -> SynchronizedScopeBuilder(name)
    }
        .apply(configuration)
        .build()
}

/**
 * This class hols set of instance creation rules – factories
 * and grants access to that factories via [Accessor] instance.
 */
class Scope internal constructor(
    val name: String,
    val parents: List<Scope>,
    private val objectFactories: ObjectFactoryMap,
    private val collectionFactories: Map<CollectionKey, List<InstanceFactory<*>>>,
    private val associationFactories: Map<AssociationKey, List<InstanceFactory<out Pair<Any, Any>>>>,
    private val allowedObjectOverrides: Set<ObjectKey>,
) {

    /**
     * Grants access to existing factories.
     */
    @PublishedApi
    internal val accessor: Accessor = if (Scout.Optimizations.disableInterceptors) {
        DirectAccessor(this)
    } else {
        TrickyAccessor(this)
    }

    /**
     * Contains all distinct flattened scope parents in
     * reverse preorder traversal order.
     */
    internal val nrlParentLookup = nrlParentLookup(parents)

    /**
     * Contains all distinct flattened scope parents in
     * postorder traversal order.
     */
    internal val lrnParentLookup = lrnParentLookup(parents)

    /**
     * List of all distinct parent scopes (including nested parent scopes)
     * in order appropriate for searching object factories.
     */
    internal val objectsParentLookup = nrlParentLookup

    /**
     * List of all distinct parent scopes (including nested parent scopes)
     * in order appropriate for searching collection factories.
     */
    internal val collectionsParentLookup = lrnParentLookup

    /**
     * List of all distinct parent scopes (including nested parent scopes)
     * in order appropriate for searching association factories.
     */
    internal val associationsParentLookup = nrlParentLookup

    /**
     * Cached parent lookups size.
     */
    private val parentLookupSize = nrlParentLookup.size

    internal fun getObjectProvider(key: ObjectKey, required: Boolean): Provider<*> {
        return Provider { getObject(key, required) }
    }

    internal fun getObjectLazy(key: ObjectKey, required: Boolean): Lazy<*> {
        return lazy { getObject(key, required) }
    }

    internal fun getCollectionProvider(key: CollectionKey, nonEmpty: Boolean): Provider<List<*>> {
        return Provider { getCollection(key, nonEmpty) }
    }

    internal fun getCollectionLazy(key: CollectionKey, nonEmpty: Boolean): Lazy<List<*>> {
        return lazy { getCollection(key, nonEmpty) }
    }

    internal fun getAssociationProvider(key: AssociationKey, nonEmpty: Boolean): Provider<Map<*, *>> {
        return Provider { getAssociation(key, nonEmpty) }
    }

    internal fun getAssociationLazy(key: AssociationKey, nonEmpty: Boolean): Lazy<Map<*, *>> {
        return lazy { getAssociation(key, nonEmpty) }
    }

    /**
     * Looking for an object factory and tries to get an object from it.
     * - throws exception if object instantiation failed
     * - throws exception if instance required but factory is missing
     * - throws exception if instance required but factory returns null
     */
    internal fun getObject(key: ObjectKey, required: Boolean): Any? {
        val factory = findSelfDefinedObjectFactory(key)
        if (factory != null) {
            return tryGetObject(key, factory, accessor, required)
        }
        objectsParentLookup.forEachParent { parent ->
            val parentFactory = parent.findSelfDefinedObjectFactory(key)
            if (parentFactory != null) {
                return tryGetObject(key, parentFactory, parent.accessor, required)
            }
        }
        if (!required) {
            return null
        }
        throw MissingObjectFactoryException(
            key = key,
            scope = this,
        )
    }

    /**
     * Looking for an element factories and tries to get elements from it.
     * - throws exception if element instantiation failed
     * - throws exception if collection is required but elements is missing
     */
    internal fun getCollection(key: CollectionKey, required: Boolean): List<*> {
        val elements = mutableListOf<Any?>()
        collectionsParentLookup.forEachParent { parent ->
            val parentFactories = parent.findSelfDefinedElementFactories(key)
            for (i in parentFactories.indices) {
                elements += tryGetElement(key, parentFactories[i], parent.accessor)
            }
        }
        val selfFactories = findSelfDefinedElementFactories(key)
        for (i in selfFactories.indices) {
            elements += tryGetElement(key, selfFactories[i], accessor)
        }
        if (elements.isNotEmpty() || !required) {
            return elements
        }
        throw MissingCollectionElementsException(
            key = key,
            scope = this,
        )
    }

    /**
     * Looking for a mapping factories and tries to get mappings from it.
     * - throws exception if mapping instantiation failed
     * - throws exception if association is required but mappings is missing
     */
    internal fun getAssociation(key: AssociationKey, required: Boolean): Map<*, *> {
        val mappings = mutableListOf<Pair<Any, Any>>()
        val selfFactories = findSelfDefinedMappingFactories(key)
        for (i in selfFactories.indices) {
            mappings += tryGetMapping(key, selfFactories[i], accessor)
        }
        associationsParentLookup.forEachParent { parent ->
            val parentFactories = parent.findSelfDefinedMappingFactories(key)
            for (i in parentFactories.indices) {
                mappings += tryGetMapping(key, parentFactories[i], parent.accessor)
            }
        }
        if (mappings.isNotEmpty() || !required) {
            return mappings.toMap()
        }
        throw MissingMappingException(
            key = key,
            scope = this,
        )
    }

    /**
     * Looking for an object factory by specified [ObjectKey] in [objectFactories].
     * Returns null if factory by specified key is absent.
     */
    private fun findSelfDefinedObjectFactory(key: ObjectKey): InstanceFactory<*>? {
        return objectFactories[key]
    }

    /**
     * Looking for an element factories by specified [CollectionKey] in [collectionFactories].
     * Returns empty list if factories by specified key is absent.
     */
    private fun findSelfDefinedElementFactories(key: CollectionKey): List<InstanceFactory<*>> {
        return collectionFactories[key] ?: emptyList()
    }

    /**
     * Looking for an association factories by specified [AssociationKey] in [associationFactories].
     * Returns empty list if factories by specified key is absent.
     */
    private fun findSelfDefinedMappingFactories(key: AssociationKey): List<InstanceFactory<out Pair<Any, Any>>> {
        return associationFactories[key] ?: emptyList()
    }

    /**
     * Tries to get an object from [factory] and returns it
     * otherwise wraps thrown exception into [ObjectCreationFailedException].
     * If object is required and instance is null throws [ObjectNullabilityException].
     */
    private fun tryGetObject(
        key: ObjectKey,
        factory: InstanceFactory<*>,
        accessor: Accessor,
        required: Boolean
    ): Any? {
        val instance = try {
            factory.get(accessor)
        } catch (exception: Exception) {
            throw ObjectCreationFailedException(
                key = key,
                scope = this,
                cause = exception,
            )
        }
        if (instance == null && required) {
            throw ObjectNullabilityException(
                key = key,
                scope = this,
            )
        }
        return instance
    }

    /**
     * Tries to get an element from [factory] and returns it
     * otherwise wraps thrown exception into [ElementCreationFailedException].
     */
    private fun tryGetElement(
        key: CollectionKey,
        factory: InstanceFactory<*>,
        accessor: Accessor
    ): Any? {
        return try {
            factory.get(accessor)
        } catch (exception: Exception) {
            throw ElementCreationFailedException(
                key = key,
                scope = this,
                cause = exception,
            )
        }
    }

    /**
     * Tries to get a mapping from [factory] and returns it
     * otherwise wraps thrown exception into [MappingCreationFailedException].
     */
    private fun tryGetMapping(
        key: AssociationKey,
        factory: InstanceFactory<out Pair<Any, Any>>,
        accessor: Accessor
    ): Pair<Any, Any> {
        return try {
            factory.get(accessor)
        } catch (exception: Exception) {
            throw MappingCreationFailedException(
                key = key,
                scope = this,
                cause = exception,
            )
        }
    }

    private inline fun List<Scope>.forEachParent(usage: (Scope) -> Unit) {
        for (i in 0 until parentLookupSize) {
            usage(this[i])
        }
    }

    /**
     * Check if scope contains object factory for [key].
     */
    internal fun containsObjectFactory(key: ObjectKey): Boolean {
        return findSelfDefinedObjectFactory(key) != null ||
                objectsParentLookup.any { parent ->
                    parent.findSelfDefinedObjectFactory(key) != null
                }
    }

    /**
     * Check if scope contains element factories for [key].
     */
    internal fun containsElementFactories(key: CollectionKey): Boolean {
        return findSelfDefinedElementFactories(key).isNotEmpty() ||
                collectionsParentLookup.any { parent ->
                    parent.findSelfDefinedElementFactories(key).isNotEmpty()
                }
    }

    /**
     * Check if scope contains mapping factories for [key].
     */
    internal fun containsMappingFactories(key: AssociationKey): Boolean {
        return findSelfDefinedMappingFactories(key).isNotEmpty() ||
                associationsParentLookup.any { parent ->
                    parent.findSelfDefinedMappingFactories(key).isNotEmpty()
                }
    }

    override fun toString() = ScopeUtils.formatIdentity(name)

    fun details(): String {
        val indent = "   "
        fun StringBuilder.inlineScope(node: Scope, indent: String, depth: Int) {
            appendLine()
            repeat(depth + 1) {
                append(indent)
            }
            append(
                "⌞ $node (" +
                        "object factories: ${node.objectFactories.toMap().size}, " +
                        "collection factories: ${node.collectionFactories.size}, " +
                        "association factories: ${node.associationFactories.size}, " +
                        "allowed object overrides: ${node.allowedObjectOverrides.size}" +
                        ")"
            )
            for (dependency in node.objectsParentLookup) {
                inlineScope(dependency, indent, depth + 1)
            }
        }
        return buildString {
            append("\nTree of scopes:")
            inlineScope(this@Scope, indent, 0)
        }
    }
}
