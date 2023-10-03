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
package scout.inspector

import scout.Scope
import scout.definition.AssociationKey
import scout.definition.CollectionKey
import scout.definition.ObjectKey
import scout.factory.InstanceFactory
import java.lang.IllegalArgumentException
import java.util.Collections

/**
 * Allows to inspect scope content. Uses reflection for field revelation,
 * so you should not use inspects in obfuscated build configurations.
 */
class ScopeInspector(
    val scope: Scope,
    val objectFactories: Map<ObjectKey, InstanceFactory<*>>,
    val collectionFactories: Map<CollectionKey, List<InstanceFactory<*>>>,
    val associationFactories: Map<AssociationKey, List<InstanceFactory<out Pair<Any, Any>>>>,
    val allowedObjectOverrides: Set<ObjectKey>
)

/**
 * Creates scope inspector with unmodifiable field values.
 */
fun Scope.inspect() = ScopeInspector(
    scope = this,
    objectFactories = unmodifiable(revealField(this, "objectFactories")),
    collectionFactories = unmodifiable(revealField(this, "collectionFactories")),
    associationFactories = unmodifiable(revealField(this, "associationFactories")),
    allowedObjectOverrides = unmodifiable(revealField(this, "allowedObjectOverrides")),
)

private inline fun <reified T> revealField(instance: Any, fieldName: String): T {
    val field = instance::class.java.getDeclaredField(fieldName)
    field.isAccessible = true
    return field.get(instance) as T
}

private inline fun <reified T> unmodifiable(value: T): T {
    return when (value) {
        is Map<*, *> -> Collections.unmodifiableMap(value) as T
        is Set<*> -> Collections.unmodifiableSet(value) as T
        else -> throw IllegalArgumentException(
            "Missing unmodifiable implementation for ${value!!::class.qualifiedName}"
        )
    }
}
