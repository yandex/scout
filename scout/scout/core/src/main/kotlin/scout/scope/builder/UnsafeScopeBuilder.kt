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
package scout.scope.builder

import scout.Scope
import scout.Scout
import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey
import scout.exception.IllegalOverridesException
import scout.exception.ScopeInitializationException
import scout.factory.InstanceFactory
import scout.logging.Logger
import scout.map.createObjectFactoryMap

@PublishedApi
internal open class UnsafeScopeBuilder(
    private val name: String
) : ScopeBuilder() {
    private var logger = Scout.defaultLogger
    private val parentScopes = mutableListOf<Scope>()
    private val objectFactories = HashMap<ObjectKey, InstanceFactory<*>>()
    private val collectionFactories = HashMap<CollectionKey, MutableList<InstanceFactory<*>>>()
    private val associationFactories = HashMap<AssociationKey, MutableList<InstanceFactory<out Pair<Any, Any>>>>()
    private val allowedObjectOverrides = hashSetOf<ObjectKey>()
    private val illegalOverrides = mutableListOf<ObjectKey>()

    override fun dependsOn(parent: Scope) {
        parentScopes += parent
    }

    override fun logger(logger: Logger?) {
        this.logger = logger
    }

    override fun saveObjectMapping(
        definitionKey: ObjectKey,
        factory: InstanceFactory<*>,
        allowOverride: Boolean,
    ) {
        if (objectFactories.put(definitionKey, factory) != null && !allowOverride) {
            illegalOverrides.add(definitionKey)
        }
        if (allowOverride) {
            allowedObjectOverrides.add(definitionKey)
        }
    }

    override fun saveCollectionMapping(
        definitionKey: CollectionKey,
        factory: InstanceFactory<*>,
    ) {
        collectionFactories.getOrPut(definitionKey) {
            mutableListOf()
        }.add(factory)
    }

    override fun saveAssociationMapping(
        definitionKey: AssociationKey,
        factory: InstanceFactory<out Pair<Any, Any>>,
    ) {
        associationFactories.getOrPut(definitionKey) {
            mutableListOf()
        }.add(factory)
    }

    private fun checkThereWasNoIllegalOverrides(illegalOverrides: Collection<ObjectKey>) {
        if (illegalOverrides.isNotEmpty()) {
            throw IllegalOverridesException(
                keys = illegalOverrides,
                scopeName = name,
            )
        }
    }

    override fun build(): Scope {
        return try {
            logger?.info { "Start initialization of scope \"$name\"" }
            checkThereWasNoIllegalOverrides(illegalOverrides)
            val scope = Scope(
                name = name,
                parents = parentScopes,
                objectFactories = createObjectFactoryMap(objectFactories),
                collectionFactories = collectionFactories,
                associationFactories = associationFactories,
                allowedObjectOverrides = allowedObjectOverrides,
            )
            logger?.info { "Finish initialization of \"$name\"" }
            scope
        } catch (error: Exception) {
            throw ScopeInitializationException(
                name = name,
                cause = error,
            )
        }
    }
}
