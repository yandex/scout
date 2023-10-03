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
package scout.validator.tools.method

import scout.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * This interface allows to invoke methods in generified manner.
 * Call argument policy depends on implementation.
 */
fun interface MethodInvoker {

    /**
     * Invoke specified [method] on passed [receiver].
     */
    fun invoke(receiver: Component, method: Method): Any?

    companion object {

        /**
         * Default invoker implementation with [argumentProducer] for call arguments.
         */
        fun default(argumentProducer: MethodArgumentProducer<in Component>?) = object : MethodInvoker {
            override fun invoke(
                receiver: Component,
                method: Method,
            ): Any? {
                val parameters = method.parameterTypes.map { type ->
                    argumentProducer?.produce(receiver, method, type)
                }.toTypedArray()
                try {
                    method.isAccessible = true
                    return method.invoke(receiver, *parameters)
                } catch (e: InvocationTargetException) {
                    throw e.targetException
                }
            }
        }
    }
}
