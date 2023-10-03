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
import graph.generator.GraphConfig
import graph.generator.GraphGenerator
import graph.generator.GraphSourcesGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.random.Random

private const val GRAPHS_PACKAGE = "graphs"

open class GenerateGraphTask : DefaultTask() {

    @Input
    lateinit var configurations: List<GraphConfiguration>

    @OutputDirectory
    lateinit var output: File

    @TaskAction
    fun generate() {
        output.deleteRecursively()
        output.mkdirs()
        for (configuration in configurations) {
            val random = if (configuration.random) Random(System.nanoTime()) else Random(42)
            val config = GraphConfig(
                name = configuration.name,
                roots = configuration.roots,
                nodes = configuration.nodes,
                chanceToHaveNeighbourLeaf = .05,
                moduleSize = configuration.modulesMinSize..configuration.modulesMaxSize
            )
            val graph = GraphGenerator.generate(config, random)
            GraphSourcesGenerator.generate(graph, output, GRAPHS_PACKAGE)
        }
    }
}
