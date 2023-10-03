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
package scout.benchmark.benchmarks.assessment

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import scout.Component
import scout.benchmark.DEFAULT_FORKS
import scout.benchmark.DEFAULT_MEASURE_ITERATIONS
import scout.benchmark.DEFAULT_MEASURE_SECONDS
import scout.benchmark.DEFAULT_WARMUP_ITERATIONS
import scout.benchmark.DEFAULT_WARMUP_SECONDS
import scout.benchmark.Optimized
import scout.scope
import java.util.concurrent.TimeUnit

@Fork(DEFAULT_FORKS)
@Warmup(iterations = DEFAULT_WARMUP_ITERATIONS, time = DEFAULT_WARMUP_SECONDS)
@Measurement(iterations = DEFAULT_MEASURE_ITERATIONS, time = DEFAULT_MEASURE_SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class ParentAccessBenchmark : Optimized() {

    @Benchmark
    fun withoutParents(blackhole: Blackhole) {
        blackhole.consume(ComponentWithoutParents.number())
    }

    @Benchmark
    fun with1Parent(blackhole: Blackhole) {
        blackhole.consume(ComponentWithSingleParent.number())
    }

    @Benchmark
    fun with5Parents(blackhole: Blackhole) {
        blackhole.consume(ComponentWith5Parents.number())
    }

    @Benchmark
    fun with2Depth(blackhole: Blackhole) {
        blackhole.consume(ComponentWith2Depth.number())
    }

    @Benchmark
    fun with3Depth(blackhole: Blackhole) {
        blackhole.consume(ComponentWith3Depth.number())
    }
}

private val scopeWithNumber = scope("") {
    factory<Int> { 42 }
}

private val dummyScope1 = scope("") { }
private val dummyScope2 = scope("") { }
private val dummyScope3 = scope("") { }
private val dummyScope4 = scope("") { }

private val scopeWithSingleParent = scope("") {
    dependsOn(scopeWithNumber)
}

private val scopeWith5Parents = scope("") {
    dependsOn(scopeWithNumber)
    dependsOn(dummyScope1)
    dependsOn(dummyScope2)
    dependsOn(dummyScope3)
    dependsOn(dummyScope4)
}

private val scopeWith2Depth = scope("") {
    dependsOn(scopeWithSingleParent)
}

private val scopeWith3Depth = scope("") {
    dependsOn(scopeWith2Depth)
}

private object ComponentWithoutParents : Component(scopeWithNumber) {
    fun number(): Int = get()
}

private object ComponentWithSingleParent : Component(scopeWithSingleParent) {
    fun number(): Int = get()
}

private object ComponentWith5Parents : Component(scopeWith5Parents) {
    fun number(): Int = get()
}

private object ComponentWith2Depth : Component(scopeWith2Depth) {
    fun number(): Int = get()
}

private object ComponentWith3Depth : Component(scopeWith3Depth) {
    fun number(): Int = get()
}
