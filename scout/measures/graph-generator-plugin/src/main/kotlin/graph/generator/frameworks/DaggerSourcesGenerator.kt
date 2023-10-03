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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graph.generator.Graph
import graph.generator.nodeClassName

internal object DaggerSourcesGenerator : AbstractSourcesGenerator("dagger") {

    override fun generateModule(
        builder: FileSpec.Builder,
        graph: Graph,
        module: Int,
        nodes: List<Int>,
        packageName: String
    ) {
        val classBuilder = TypeSpec.classBuilder("Module$module")
            .addAnnotation(ClassName("dagger", "Module"))

        nodes.forEach { node ->
            val dependencies = graph.neighbours[node]!!

            val funBuilder = FunSpec.builder("provideNode$node")
                .returns(nodeClassName(packageName, node))
                .addStatement("return Node$node(${dependencies.joinToString(", ") { "node$it" }})")
                .addAnnotation(ClassName("dagger", "Provides"))

            dependencies.forEach { dependency ->
                funBuilder.addParameter("node$dependency", nodeClassName(packageName, dependency))
            }

            classBuilder.addFunction(funBuilder.build())
        }

        builder.addType(classBuilder.build())
    }

    override fun generateComponent(
        builder: FileSpec.Builder,
        graph: Graph,
        packageName: String
    ) {
        val componentBuilder = TypeSpec.interfaceBuilder("DaggerComponent")
            .addAnnotation(
                AnnotationSpec.builder(ClassName("dagger", "Component"))
                    .addMember("modules = [\n\t${graph.modules.indices.joinToString("\n\t") { "Module$it::class," }}\n]")
                    .build()
            )

        for (module in graph.modules.indices) {
            builder.addImport("$packageName.dagger", "Module$module")
        }

        for (root in 0 until graph.config.roots) {
            componentBuilder.addFunction(
                FunSpec.builder("getNode$root")
                    .addModifiers(KModifier.ABSTRACT)
                    .returns(nodeClassName(packageName, root))
                    .build()
            )
        }

        builder.addType(componentBuilder.build())

        builder.addProperty(
            PropertySpec.builder("graph", ClassName("$packageName.dagger", "DaggerComponent"))
                .initializer("DaggerDaggerComponent.create()")
                .build()
        )

        for (root in 0 until graph.config.roots) {
            builder.addFunction(
                FunSpec.builder("daggerGetNode$root")
                    .returns(nodeClassName(packageName, root))
                    .addCode("return graph.getNode$root()")
                    .build()
            )
        }

        builder.addFunction(
            FunSpec.builder("daggerInitGraph")
                .returns(ClassName("$packageName.dagger", "DaggerComponent"))
                .addCode("return graph")
                .build()
        )
    }
}
