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

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import scout.scope.access.DirectAccessor
import scout.scope.container.lateInitScope

class LateInitScopeTest {

    @Test
    fun `Stores scope configured with init block`() {
        val lateInitScope = lateInitScope(name = "testScope")
        lateInitScope.init {
            factory<String> { "test-string" }
        }
        val accessor = DirectAccessor(lateInitScope.value)
        assertThat(accessor.get<String>()).isEqualTo("test-string")
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun `Throw exception on reading not yet initialized scope`() {
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            val scope = lateInitScope(name = "testScope", isOverwriteAllowed = false)
            val value = scope.value
        }.withMessageContaining("was not initialized")
    }

    @Test
    fun `Throw exception when writing over initialized value and overwrite is not allowed`() {
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            val scope = lateInitScope(name = "testScope", isOverwriteAllowed = false)
            scope.init {}
            scope.init {}
        }.withMessageContaining("is already initialized")
    }

    @Test
    fun `Don't throw exception when writing over initialized value and overwrite is allowed`() {
        assertThatNoException().isThrownBy {
            val scope = lateInitScope(name = "testScope", isOverwriteAllowed = true)
            scope.init {}
            scope.init {}
        }
    }
}
