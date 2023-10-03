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

import com.squareup.kotlinpoet.FileSpec
import graph.generator.Graph
import java.io.File

internal abstract class AbstractSourcesGenerator(private val framework: String) {

    protected abstract fun generateModule(
        builder: FileSpec.Builder,
        graph: Graph,
        module: Int,
        nodes: List<Int>,
        packageName: String
    )

    protected abstract fun generateComponent(
        builder: FileSpec.Builder,
        graph: Graph,
        packageName: String
    )

    fun generate(graph: Graph, folder: File, packageName: String) {
        val frameworkPackage = "$packageName.$framework"
        graph.modules.forEachIndexed { module, nodes ->
            val moduleSpec = FileSpec
                .builder(frameworkPackage, "Module$module")
            generateModule(
                moduleSpec,
                graph,
                module,
                nodes,
                packageName
            )
            moduleSpec.build().writeTo(folder)
        }

        val componentSpec = FileSpec.builder(frameworkPackage, "Component")
        generateComponent(componentSpec, graph, packageName)
        componentSpec.build().writeTo(folder)
    }
}
