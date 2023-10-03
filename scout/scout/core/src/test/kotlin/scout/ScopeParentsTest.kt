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

class ScopeParentsTest {

    @Test
    fun `Prepare proper object factories lookup`() {
        val scope = scope("") {
            dependsOn(scope3)
            dependsOn(scope5)
        }
        val lookup = scope.objectsParentLookup
        assertThat(lookup).containsExactly(scope5, scope4, scope1, scope3, scope2)
    }

    @Test
    fun `Prepare proper mapping factories lookup`() {
        val scope = scope("") {
            dependsOn(scope3)
            dependsOn(scope5)
        }
        val lookup = scope.associationsParentLookup
        assertThat(lookup).containsExactly(scope5, scope4, scope1, scope3, scope2)
    }

    @Test
    fun `Prepare proper element factories lookup`() {
        val scope = scope("") {
            dependsOn(scope3)
            dependsOn(scope5)
        }
        val lookup = scope.collectionsParentLookup
        assertThat(lookup).containsExactly(scope1, scope2, scope3, scope4, scope5)
    }

    companion object {
        val scope1 = scope("scope-1") {}
        val scope2 = scope("scope-2") {}
        val scope3 = scope("scope-3") {
            dependsOn(scope1)
            dependsOn(scope2)
        }
        val scope4 = scope("scope-4") {}
        val scope5 = scope("scope-5") {
            dependsOn(scope1)
            dependsOn(scope4)
        }
    }
}
