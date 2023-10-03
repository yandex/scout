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

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graph.generator.frameworks.DaggerSourcesGenerator
import graph.generator.frameworks.KatanaSourcesGenerator
import graph.generator.frameworks.KodeinSourcesGenerator
import graph.generator.frameworks.KoinSourcesGenerator
import graph.generator.frameworks.ScoutSourcesGenerator
import java.io.File

object GraphSourcesGenerator {

    private val frameworks = listOf(
        ScoutSourcesGenerator,
        KoinSourcesGenerator,
        KodeinSourcesGenerator,
        KatanaSourcesGenerator,
        DaggerSourcesGenerator
    )

    fun generate(graph: Graph, folder: File, packageName: String) {
        val graphPackage = "$packageName.${graph.config.name}"
        generateNodes(graph, folder, graphPackage)
        generateGraphs(graph, folder, graphPackage)
    }

    private fun generateNodes(graph: Graph, folder: File, packageName: String) {
        repeat(graph.config.nodes) { node ->
            val builder = FunSpec.constructorBuilder()
            val classBuilder = TypeSpec.classBuilder("Node$node")

            graph.neighbours[node]!!.forEach { child ->
                val name = "node$child"
                val className = nodeClassName(packageName, child)
                builder.addParameter(
                    name,
                    className,
                )
                classBuilder.addProperty(
                    PropertySpec.builder(name, className)
                        .initializer(name)
                        .build()
                )
            }

            val file = FileSpec.builder("$packageName.nodes", "Node$node")
                .addType(
                    classBuilder
                        .primaryConstructor(builder.build())
                        .build()
                )
                .build()

            file.writeTo(folder)
        }
    }

    private fun generateGraphs(graph: Graph, folder: File, packageName: String) {
        for (framework in frameworks) {
            framework.generate(graph, folder, packageName)
        }
    }
}
