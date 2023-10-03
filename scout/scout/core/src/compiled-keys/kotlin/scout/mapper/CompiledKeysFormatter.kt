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
package scout.mapper

import java.util.concurrent.atomic.AtomicBoolean

internal class CompiledKeysFormatter(private val compileKeyMapper: CompiledKeyMapper) : KeysMap {

    private val objectKeys = HashMap<Int, String>()
    private val collectionKeys = HashMap<Int, String>()
    private val associationKeys = HashMap<Int, Pair<String, String>>()
    private val initialized = AtomicBoolean(false)

    private fun initMapping() {
        if (!initialized.getAndSet(true)) {
            compileKeyMapper.__init_mapping__(this)
        }
    }

    override fun putObjectKey(key: Int, className: String) {
        objectKeys[key] = className
    }

    override fun putCollectionKey(key: Int, className: String) {
        collectionKeys[key] = className
    }

    override fun putAssociationKey(key: Int, keyClassName: String, valueClassName: String) {
        associationKeys[key] = keyClassName to valueClassName
    }

    fun formatObjectKey(key: Int): String? {
        initMapping()
        return objectKeys[key]
    }

    fun formatCollectionKey(key: Int): String? {
        initMapping()
        return collectionKeys[key]
    }

    fun formatAssociationKey(key: Int): Pair<String, String>? {
        initMapping()
        return associationKeys[key]
    }
}
