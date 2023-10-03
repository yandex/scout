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
import graph.generator.Graph
import graph.generator.importNode
import graph.generator.nodeClassName

internal object KodeinSourcesGenerator : AbstractSourcesGenerator("kodein") {

    override fun generateModule(
        builder: FileSpec.Builder,
        graph: Graph,
        module: Int,
        nodes: List<Int>,
        packageName: String
    ) {
        val factories = nodes.joinToString("\n") { node ->
            "bindProvider { Node$node(${graph.neighbours[node]!!.joinToString(", ") { "instance()" } }) }"
        }
        val propertyBuilder = PropertySpec.builder(
            "module$module", ClassName("org.kodein.di", "DI", "Module")
        ).initializer("DI.Module(\"module$module\") {\n$factories\n}")

        for (node in nodes) {
            builder.importNode(packageName, node)
        }

        builder.addImport("org.kodein.di", "bindProvider")
        builder.addImport("org.kodein.di","instance")
        builder.addImport("org.kodein.di", "DI")
        builder.addProperty(propertyBuilder.build())
    }

    override fun generateComponent(builder: FileSpec.Builder, graph: Graph, packageName: String) {
        val modules = graph.modules.indices.joinToString("\n\t") { "module$it," }
        builder.addProperty(
            PropertySpec.builder("graph", ClassName("org.kodein.di", "DirectDI"))
                .initializer("DI {\nimportAll(\n\t$modules\n)\n}.direct")
                .build()
        )

        for (root in 0 until graph.config.roots) {
            builder.addFunction(
                FunSpec.builder("kodeinGetNode$root")
                    .returns(nodeClassName(packageName, root))
                    .addCode("return graph.instance<Node$root>()")
                    .build()
            )
        }

        builder.addFunction(
            FunSpec.builder("kodeinInitGraph")
                .returns(ClassName("org.kodein.di", "DirectDI"))
                .addCode("return graph")
                .build()
        )

        builder.addImport("org.kodein.di", "DI")
        builder.addImport("org.kodein.di", "DirectDI")
        builder.addImport("org.kodein.di", "direct")
        builder.addImport("org.kodein.di", "instance")
    }
}
