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
package scout.builtin

import scout.Provider
import scout.definition.ObjectKey
import scout.definition.ObjectKeys

object BuiltinKeys {
    val OBJECT_KEY_LAZY: ObjectKey = ObjectKeys.create(Lazy::class.java)
    val OBJECT_KEY_PROVIDER: ObjectKey = ObjectKeys.create(Provider::class.java)
    val OBJECT_KEY_UNIT: ObjectKey = ObjectKeys.create(Unit::class.java)
}
