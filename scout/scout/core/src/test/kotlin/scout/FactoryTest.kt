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
import org.junit.jupiter.api.Test
import scout.scope.access.DirectAccessor

class FactoryTest {

    private class Instance {
        val id: Int = nextInstanceId++

        companion object {
            private var nextInstanceId = 1
        }
    }

    @Test
    fun `Factory creates new instance each time`() {
        val scope = scope("test-scope") {
            factory<Instance> { Instance() }
        }
        val accessor = DirectAccessor(scope)
        val calls = 100
        val instances = (0 until calls).map {
            accessor.get<Instance>()
        }
        val ids = instances.map { instance -> instance.id }.toSet()
        assertThat(ids.size).isEqualTo(instances.size).isEqualTo(calls)
    }

    @Test
    fun `Singleton creates single instance`() {
        val scope = scope("test-scope") {
            singleton<Instance> { Instance() }
        }
        val accessor = DirectAccessor(scope)
        val instances = (0 until 100).map {
            accessor.get<Instance>()
        }
        val ids = instances.map { instance -> instance.id }.toSet()
        assertThat(ids.size).isEqualTo(1)
    }

    @Test
    fun `Reusable creates less instances then calls`() {
        val scope = scope("test-scope") {
            reusable<Instance> { Instance() }
        }
        val accessor = DirectAccessor(scope)
        val instances = (0 until 100).map {
            accessor.get<Instance>()
        }
        val ids = instances.map { instance -> instance.id }.toSet()
        assertThat(ids.size).isLessThan(instances.size)
    }
}
