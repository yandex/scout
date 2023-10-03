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
package scout

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import scout.scope.builder.ScopeBuilder
import scout.utils.runBlockingInDifferentThread
import java.util.concurrent.ExecutionException

class ScopeBuilderTest {

    @Test
    fun `Don't throw exception when called from multiple threads in unsafe mode`() {
        assertThatNoException().isThrownBy {
            scope(
                name = "testScope",
                threadSafetyMode = ScopeBuilder.ThreadSafetyMode.Unsafe,
            ) {
                factory<Any> { Any() }

                runBlockingInDifferentThread {
                    element<CharSequence> { "String" }
                }
            }
        }
    }

    @Test
    fun `Throws exception when called from multiple threads in thread confinement mode`() {
        assertThatExceptionOfType(ExecutionException::class.java).isThrownBy {
            scope(
                name = "testScope",
                threadSafetyMode = ScopeBuilder.ThreadSafetyMode.Confined,
            ) {
                factory<Any> { Any() }

                runBlockingInDifferentThread {
                    element<CharSequence> { "String" }
                }
            }
        }.withCauseInstanceOf(IllegalStateException::class.java)
    }
}
