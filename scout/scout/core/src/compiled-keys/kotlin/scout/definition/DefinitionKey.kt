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
@file:Suppress("UnusedPrivateMember")

package scout.definition

import scout.compiledKeyMapper
import scout.mapper.CompiledKeysFormatter

typealias ObjectKey = Int
typealias CollectionKey = Int
typealias AssociationKey = Int

object ObjectKeys {

    @JvmStatic
    fun create(
        type: Class<*>
    ): ObjectKey {
        implError()
    }

    fun format(key: ObjectKey) = "Object(type=${keysFormatter?.formatObjectKey(key) ?: key})"

    fun getClass(key: ObjectKey): Class<*>? {
        val name = keysFormatter?.formatObjectKey(key) ?: return null
        return try {
            Class.forName(name)
        } catch (e: ClassNotFoundException) {
             null
        }
    }
}

object CollectionKeys {

    @JvmStatic
    fun create(
        type: Class<*>
    ): CollectionKey {
        implError()
    }

    fun format(key: CollectionKey) = "Collection(type=${keysFormatter?.formatCollectionKey(key) ?: key})"

    fun getClass(key: CollectionKey): Class<*>? {
        val name = keysFormatter?.formatCollectionKey(key) ?: return null
        return try {
            Class.forName(name)
        } catch (e: ClassNotFoundException) {
            null
        }
    }
}

object AssociationKeys {

    @JvmStatic
    fun create(
        keyType: Class<*>,
        valueType: Class<*>
    ): AssociationKey {
        implError()
    }

    fun format(key: AssociationKey): String {
        val classesPair = keysFormatter?.formatAssociationKey(key)
            ?: return "Association(type=$key)"

        return "Association(keyType=${classesPair.first}, valueType=${classesPair.second})"
    }

    fun getClasses(key: AssociationKey): Pair<Class<*>, Class<*>>? {
        val pair = keysFormatter?.formatAssociationKey(key) ?: return null
        return try {
            Class.forName(pair.first) to Class.forName(pair.second)
        } catch (e: ClassNotFoundException) {
            null
        }
    }
}

private val keysFormatter: CompiledKeysFormatter? by lazy {
    compiledKeyMapper?.let(::CompiledKeysFormatter)
}

private fun implError(): Nothing {
    @Suppress("NotImplementedDeclaration")
    throw NotImplementedError("Error in bytecode modification. ScoutPlugin had its job done incorrectly!")
}
