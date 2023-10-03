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
package scout.validator.utils

import java.lang.reflect.Method

internal val lineSeparator = System.lineSeparator()
internal val doubledLineSeparator = lineSeparator + lineSeparator

internal fun formatMethodRef(receiverType: Class<*>, method: Method): String {
    val isKotlinClass = receiverType.isAnnotationPresent(Metadata::class.java)
    val extension = if (isKotlinClass) "kt" else "java"
    return "${receiverType.canonicalName}.${method.name}(${receiverType.simpleName}.$extension:0)"
}
