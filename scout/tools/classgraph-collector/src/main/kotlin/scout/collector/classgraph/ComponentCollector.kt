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
package scout.collector.classgraph

import io.github.classgraph.ClassGraph
import scout.Component
import scout.Injector
import scout.validator.tools.component.ComponentProducer
import scout.validator.tools.instance.InstanceProducer
import scout.validator.tools.instance.ReflectiveInstanceProducer

/**
 * Allows to collect [Component]s from source code
 * using ClassGraph library for code scanning.
 *
 * It's strictly recommended to pass [classpathFilter]
 * to reduce component scanning scope.
 */
class ComponentCollector(
    private val packageNames: List<String> = emptyList(),
    private val classpathFilter: ClasspathFilter = ClasspathFilter { true },
    private val classLoader: ClassLoader = ClassLoader.getSystemClassLoader(),
    private val instanceProducer: InstanceProducer<Component> = ReflectiveInstanceProducer(null)
) : ComponentProducer {

    @Suppress("UNCHECKED_CAST")
    override fun produce(): Iterable<Component> {
        val scanned = ClassGraph()
            .enableClassInfo()
            .ignoreClassVisibility()
            .filterPackages(packageNames)
            .filterClasspathElements { classpath -> classpathFilter.matches(classpath) }
            .scan()
        val components = scanned.getSubclasses(Component::class.java)
            .map { classInfo -> classLoader.loadClass(classInfo.name) }
            .map { clazz -> clazz as Class<Component> }
            .filter { clazz -> clazz != Injector::class.java }
        val injectors = scanned.getSubclasses(Injector::class.java)
            .map { classInfo -> classLoader.loadClass(classInfo.name) }
            .map { clazz -> clazz as Class<Component> }
        return (components + injectors)
            .also { instances -> require(instances.isNotEmpty()) { "Missing components" } }
            .map { clazz -> instanceProducer.produce(clazz) }
    }

    private fun ClassGraph.filterPackages(packageNames: List<String>): ClassGraph {
        if (packageNames.isNotEmpty()) {
            return this.acceptPackages(*packageNames.toTypedArray())
        }
        return this
    }
}
