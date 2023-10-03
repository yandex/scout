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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import scout.exception.ObjectCreationFailedException
import scout.utils.test

class ScopeIsolationTest {

    @Test
    fun `Check parent scope has no access to object definitions of child scope`() {
        val coreScope = scope("core") {
            factory<NetworkClient> {
                NetworkClient(singleSetting = get())
            }
        }
        val featureScope = scope("feature") {
            dependsOn(coreScope)
            factory<NetworkSetting> {
                NetworkSetting()
            }
        }

        featureScope.test {
            assertDoesNotThrow {
                getOrThrow<NetworkSetting>()
            }
            assertThrows<ObjectCreationFailedException> {
                getOrThrow<NetworkClient>()
            }
        }
    }

    @Test
    fun `Check parent scope has no access to element definitions of child scope`() {
        val coreScope = scope("core") {
            factory<NetworkClient> {
                NetworkClient(listOfSettings = collect(nonEmpty = true))
            }
        }
        val featureScope = scope("feature") {
            dependsOn(coreScope)
            element<NetworkSetting> {
                NetworkSetting()
            }
        }

        featureScope.test {
            assertDoesNotThrow {
                collectOrThrow<NetworkSetting>()
            }
            assertThrows<ObjectCreationFailedException> {
                getOrThrow<NetworkClient>()
            }
        }
    }

    @Test
    fun `Check parent scope has no access to mapping definitions of child scope`() {
        val coreScope = scope("core") {
            factory<NetworkClient> {
                NetworkClient(mapOfSettings = associate(nonEmpty = true))
            }
        }
        val featureScope = scope("feature") {
            dependsOn(coreScope)
            mapping<String, NetworkSetting> {
                "some-setting" to NetworkSetting()
            }
        }

        featureScope.test {
            assertDoesNotThrow {
                associateOrThrow<String, NetworkSetting>()
            }
            assertThrows<ObjectCreationFailedException> {
                getOrThrow<NetworkClient>()
            }
        }
    }
}

private class NetworkSetting

@Suppress("unused")
private class NetworkClient(
    val singleSetting: NetworkSetting? = null,
    val listOfSettings: List<NetworkSetting>? = null,
    val mapOfSettings: Map<String, NetworkSetting>? = null
)
