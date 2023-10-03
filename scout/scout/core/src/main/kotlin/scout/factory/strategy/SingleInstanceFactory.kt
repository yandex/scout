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
package scout.factory.strategy

import scout.scope.access.Accessor
import scout.definition.Definition
import scout.factory.InstanceFactory

internal val UNINITIALIZED_INSTANCE = Any()

@Suppress("FunctionName")
@PublishedApi
internal inline fun <T, R> SingleInstanceFactory(
    crossinline definition: Definition<T, R>,
): InstanceFactory<R> {
    return object : SingleInstanceFactory<R>() {
        override fun factoryGet(accessor: Accessor): R {
            return definition(accessor, null)
        }
    }
}

@PublishedApi
internal abstract class SingleInstanceFactory<R> : InstanceFactory<R> {

    @Volatile
    private var instance: Any? = UNINITIALIZED_INSTANCE

    override fun get(accessor: Accessor): R {
        val existingInstance = instance
        if (existingInstance !== UNINITIALIZED_INSTANCE) {
            @Suppress("UNCHECKED_CAST")
            return existingInstance as R
        }
        return synchronized(this) {
            val synchronizedExistingInstance = instance
            if (synchronizedExistingInstance !== UNINITIALIZED_INSTANCE) {
                @Suppress("UNCHECKED_CAST")
                synchronizedExistingInstance as R
            } else {
                val newInstance = factoryGet(accessor)
                instance = newInstance
                newInstance
            }
        }
    }

    abstract fun factoryGet(accessor: Accessor): R
}
