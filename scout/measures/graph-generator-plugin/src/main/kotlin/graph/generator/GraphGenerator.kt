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
package graph.generator

import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

object GraphGenerator {

    fun generate(config: GraphConfig, random: Random = Random): Graph {
        var nextChildIndex = config.roots
        val neighbours = HashMap<Int, ArrayList<Int>>()
        val heights = IntArray(config.nodes)
        val nodesByHeight = HashMap<Int, ArrayList<Int>>()

        loop@ for (node in 0 until config.nodes) {
            val generatedLeavesCount = generateChildrenCount(random)

            val childrenHeight = heights[node] + 1

            for (i in 0 until generatedLeavesCount) {
                val child = if (random.nextDouble() < config.chanceToHaveNeighbourLeaf && childrenHeight in nodesByHeight) {
                    val rand = nodesByHeight[childrenHeight]!!.random(random)
                    if (rand >= node) {
                        nextChildIndex++
                    } else {
                        rand
                    }
                } else {
                    nextChildIndex++
                }

                if (child == config.nodes) {
                    break@loop
                }

                neighbours.add(node, child)
                heights[child] = childrenHeight
                nodesByHeight.add(childrenHeight, child)
            }
        }

        val modules = ArrayList<List<Int>>()

        var copied = 0
        val shuffled = (0 until config.nodes)
            .toList()
            .shuffled(random)

        while (copied < shuffled.size) {
            val moduleSize = random.nextInt(config.moduleSize)
            modules.add(shuffled.slice(copied until min(copied + moduleSize, shuffled.size)))
            copied += moduleSize
        }

        for (node in 0 until config.nodes) {
            if (node !in neighbours) {
                neighbours[node] = ArrayList()
            }
        }

        return Graph(
            config,
            neighbours,
            modules
        )
    }
}

private fun generateChildrenCount(random: Random): Int {
    val x = random.nextDouble()
    return when {
        x < .5 -> 1
        x < .8 -> 2
        x < .95 -> 3
        else -> 4
    } + 2
}

private fun <K, V> MutableMap<K, ArrayList<V>>.add(key: K, value: V) {
    val list = this[key] ?: ArrayList()
    list.add(value)
    this[key] = list
}
