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
package scout.validator.tools.instance

import java.lang.reflect.InvocationTargetException

/**
 * This class produces type instance using reflection and specified [argumentProducer].
 * - Does not work with anonymous objects and classes.
 */
class ReflectiveInstanceProducer<T>(
    private val argumentProducer: ConstructorArgumentProducer?
) : InstanceProducer<T> {

    override fun produce(type: Class<out T>): T {
        return tryObtainObjectInstance(type)
            ?: tryCreateClassInstance(type)
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryObtainObjectInstance(type: Class<out T>): T? {
        return try {
            val instance = type.getDeclaredField("INSTANCE")
            instance.isAccessible = true
            instance.get(null) as? T
        } catch (ignored: NoSuchFieldException) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryCreateClassInstance(type: Class<out T>): T {
        val constructor = type.constructors.first { constructor ->
            !constructor.isSynthetic
        }
        val parameters = constructor.parameterTypes.map { parameterType ->
            argumentProducer?.produce(type, constructor, parameterType)
        }.toTypedArray()
        return try {
            constructor.isAccessible = true
            constructor.newInstance(*parameters) as T
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }
}
