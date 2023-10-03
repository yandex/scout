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

import scout.scope.utils.ScopeUtils

internal class ScopeInitializationException(
    val name: String,
    override val cause: Throwable? = null,
) : ScoutException() {
    override val message: String
        get() = "Initialization of scope ${ScopeUtils.formatIdentity(name)} failed."
}
