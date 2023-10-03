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
package scout.exception

import scout.definition.ObjectKey
import scout.definition.ObjectKeys
import scout.scope.utils.ScopeUtils

internal class IllegalOverridesException(
    keys: Collection<ObjectKey>,
    private val scopeName: String,
    override val cause: Throwable? = null
) : ScoutException() {

    val keys: Set<ObjectKey> = keys.toSet()

    override val message: String
        get() {
            return if (keys.size == 1) {
                "Object factory for ${ObjectKeys.format(keys.first())} already exist in " +
                    "${ScopeUtils.formatIdentity(scopeName)} and override is not allowed"
            } else {
                keys.joinToString(
                    separator = System.lineSeparator(),
                    prefix = "Multiple object factories already exist in ${ScopeUtils.formatIdentity(scopeName)} " +
                            "and overrides are not allowed:${System.lineSeparator()}",
                ) { key -> "- ${ObjectKeys.format(key)}" }
            }
        }
}
