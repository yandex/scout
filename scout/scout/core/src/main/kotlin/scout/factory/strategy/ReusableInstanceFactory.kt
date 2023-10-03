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
import java.lang.ref.SoftReference

@Suppress("FunctionName")
@PublishedApi
internal inline fun <T, R> ReusableInstanceFactory(
    crossinline definition: Definition<T, R>,
): InstanceFactory<R> {
    return object : ReusableInstanceFactory<R>() {
        override fun factoryGet(accessor: Accessor): R {
            return definition(accessor, null)
        }
    }
}

@PublishedApi
internal abstract class ReusableInstanceFactory<R> : InstanceFactory<R> {

    private var instanceReference: SoftReference<R>? = null

    override fun get(accessor: Accessor): R {
        val reusableInstance = instanceReference?.get()
        return if (reusableInstance != null) {
            reusableInstance
        } else {
            val newInstance = factoryGet(accessor)
            instanceReference = newInstance?.let(::SoftReference)
            newInstance
        }
    }

    abstract fun factoryGet(accessor: Accessor): R
}
