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
package scout.scope.utils

import scout.Scope
import java.util.Collections

/**
 * Contains all distinct flattened scope parents in
 * [reverse preorder traversal order](https://en.wikipedia.org/wiki/Tree_traversal#Reverse_pre-order,_NRL).
 */
internal fun nrlParentLookup(parents: List<Scope>): List<Scope> {
    if (parents.isEmpty()) return emptyList()
    val uniqueParents = hashSetOf<Scope>()
    val lookup = mutableListOf<Scope>()

    fun appendDistinctParent(parent: Scope) {
        if (uniqueParents.add(parent)) {
            lookup += parent
        }
    }

    parents.forEachReversed { parent ->
        appendDistinctParent(parent)
        parent.nrlParentLookup.forEach { grandparent ->
            appendDistinctParent(grandparent)
        }
    }
    return lookup.toUnmodifiable()
}

/**
 * Contains all distinct flattened scope parents in
 * [postorder traversal order](https://en.wikipedia.org/wiki/Tree_traversal#Post-order,_LRN).
 */
internal fun lrnParentLookup(parents: List<Scope>): List<Scope> {
    if (parents.isEmpty()) return emptyList()
    val uniqueParents = hashSetOf<Scope>()
    val lookup = mutableListOf<Scope>()

    fun appendDistinctParent(parent: Scope) {
        if (uniqueParents.add(parent)) {
            lookup += parent
        }
    }

    parents.forEach { parent ->
        parent.lrnParentLookup.forEach { grandparent ->
            appendDistinctParent(grandparent)
        }
        appendDistinctParent(parent)
    }
    return lookup.toUnmodifiable()
}

private fun <T> List<T>.toUnmodifiable(): List<T> {
    return Collections.unmodifiableList(this)
}

private inline fun <T> List<T>.forEachReversed(action: (T) -> Unit) {
    var i = lastIndex
    while (i >= 0) {
        action(get(i))
        i--
    }
}
