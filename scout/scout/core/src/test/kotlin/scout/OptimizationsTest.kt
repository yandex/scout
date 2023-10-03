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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class OptimizationsTest {

    @AfterEach
    fun tearDown() {
        Scout.Optimizations.clear()
    }

    @Test
    fun `Check initial optimizations config`() {
        assertThat(Scout.Optimizations.disableInterceptors).isEqualTo(false)
    }

    @Test
    fun `Check that Scout#Optimizations#disableInterceptors switches corresponding flag`() {
        assertThat(Scout.Optimizations.disableInterceptors).isEqualTo(false)
        Scout.Optimizations.disableInterceptors()
        assertThat(Scout.Optimizations.disableInterceptors).isEqualTo(true)
    }

    @Test
    fun `Check that optimizations clear reverts config to initial`() {
        Scout.Optimizations.disableInterceptors()
        Scout.Optimizations.clear()
        `Check initial optimizations config`()
    }
}
