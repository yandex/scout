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
import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey
import scout.factory.InstanceFactory
import scout.logging.Logger

@PublishedApi
internal class SynchronizedScopeBuilder(
    name: String
) : UnsafeScopeBuilder(name) {

    private val lock = Any()

    override fun dependsOn(parent: Scope) {
        synchronized(lock) {
            super.dependsOn(parent)
        }
    }

    override fun logger(logger: Logger?) {
        synchronized(lock) {
            super.logger(logger)
        }
    }

    override fun saveObjectMapping(definitionKey: ObjectKey, factory: InstanceFactory<*>, allowOverride: Boolean) {
        synchronized(lock) {
            super.saveObjectMapping(definitionKey, factory, allowOverride)
        }
    }

    override fun saveCollectionMapping(definitionKey: CollectionKey, factory: InstanceFactory<*>) {
        synchronized(lock) {
            super.saveCollectionMapping(definitionKey, factory)
        }
    }

    override fun saveAssociationMapping(definitionKey: AssociationKey, factory: InstanceFactory<out Pair<Any, Any>>) {
        synchronized(lock) {
            super.saveAssociationMapping(definitionKey, factory)
        }
    }

    override fun build(): Scope {
        return synchronized(lock) {
            super.build()
        }
    }
}
