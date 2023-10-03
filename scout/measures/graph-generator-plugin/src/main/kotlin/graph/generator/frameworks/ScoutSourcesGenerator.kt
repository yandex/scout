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
package graph.generator.frameworks

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graph.generator.Graph
import graph.generator.importNode
import graph.generator.nodeClassName

internal object ScoutSourcesGenerator : AbstractSourcesGenerator("scout") {

    override fun generateModule(
        builder: FileSpec.Builder,
        graph: Graph,
        module: Int,
        nodes: List<Int>,
        packageName: String
    ) {
        val funBuilder = FunSpec.builder("module$module")
            .receiver(ClassName("scout.definition", "Registry"))
        nodes.forEach { node ->
            funBuilder.addStatement(
                "factory<Node$node> { Node$node(${graph.neighbours[node]!!.joinToString(", ") { "get()" } }) }"
            )
            builder.importNode(packageName, node)
        }
        builder.addFunction(funBuilder.build())
    }

    override fun generateComponent(
        builder: FileSpec.Builder,
        graph: Graph,
        packageName: String
    ) {
        val modules = graph.modules.indices.joinToString("\n") { "module$it()" }
        builder.addProperty(
            PropertySpec.builder("scope", ClassName("scout", "Scope"))
                .initializer("scope(\"\") {\n$modules \n}")
                .build()
        )
        val objectBuilder = TypeSpec.objectBuilder("graph")
            .superclass(ClassName("scout", "Component"))
            .addSuperclassConstructorParameter("scope")

        for (root in 0 until graph.config.roots) {
            objectBuilder.addFunction(
                FunSpec.builder("getNode$root")
                    .returns(nodeClassName(packageName, root))
                    .addCode("return get<Node$root>()")
                    .build()
            )

            builder.addFunction(
                FunSpec.builder("scoutGetNode$root")
                    .returns(nodeClassName(packageName, root))
                    .addCode("return graph.getNode$root()")
                    .build()
            )
        }

        builder.addFunction(
            FunSpec.builder("scoutInitGraph")
                .returns(ClassName("scout", "Component"))
                .addCode("return graph")
                .build()
        )

        builder.addImport("scout", "scope")
        builder.addType(objectBuilder.build())
    }
}
