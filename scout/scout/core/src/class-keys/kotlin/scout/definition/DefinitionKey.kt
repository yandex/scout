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
package scout.definition

typealias ObjectKey = Class<*>
typealias CollectionKey = Class<*>
typealias AssociationKey = Pair<Class<*>, Class<*>>

object ObjectKeys {

    fun create(
        type: Class<*>
    ): ObjectKey = type

    fun format(key: ObjectKey) = "Object(type=${key.name})"

    fun getClass(key: ObjectKey): Class<*>? = key
}

object CollectionKeys {

    fun create(
        type: Class<*>
    ): CollectionKey = type

    fun format(key: CollectionKey) = "Collection(itemType=${key.name})"

    fun getClass(key: CollectionKey): Class<*>? = key
}

object AssociationKeys {

    fun create(
        keyType: Class<*>,
        valueType: Class<*>
    ): AssociationKey = keyType to valueType

    fun format(key: AssociationKey) = "Association(keyType=${key.first.name},valueType=${key.second.name})"

    fun getClasses(key: AssociationKey): Pair<Class<*>, Class<*>>? = key
}
