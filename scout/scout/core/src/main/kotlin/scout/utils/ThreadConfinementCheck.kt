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
package scout.utils

import java.util.concurrent.atomic.AtomicReference

/**
 * This class can be used to check that some blocks of code always being called from the exact same thread.
 * For example:
 *
 *     class ThreadConfinedClass {
 *         private val checkThread = ThreadConfinementCheck()
 *
 *         private val _value = ""
 *         val value: String
 *             get() {
 *                 checkThread()
 *                 return field
 *             }
 *             set(value) {
 *                 checkThread()
 *                 field = value
 *             }
 *
 *         fun doThis() {
 *             checkThread()
 *             // ...
 *         }
 *
 *         fun doThat() {
 *             checkThread()
 *             // ...
 *         }
 *     }
 *
 * When this check is invoked from different thread than previous invocation, [IllegalStateException] is thrown with
 * error message containing both thread names.
 */
internal class ThreadConfinementCheck {
    private val boundTag = ThreadLocal<Any?>()
    private val boundThreadName = AtomicReference<String?>(null)

    operator fun invoke() {
        val threadName = Thread.currentThread().name.orEmpty()
        if (boundThreadName.compareAndSet(null, threadName)) {
            boundTag.set(Any())
        } else {
            check(boundTag.get() != null) {
                val boundThreadName = boundThreadName.get()
                """
                    Multiple threads are calling supposedly thread-confined code!
                    Initial calling thread is "$boundThreadName", and then it was called from "$threadName".
                """.trimIndent()
            }
        }
    }
}
