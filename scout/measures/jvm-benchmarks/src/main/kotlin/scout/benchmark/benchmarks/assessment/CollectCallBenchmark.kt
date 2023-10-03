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
open class CollectCallBenchmark : Optimized() {

    @Benchmark
    fun listWith1Element(blackhole: Blackhole) {
        blackhole.consume(ComponentWithNumber.number())
    }

    @Benchmark
    fun listWith10Elements(blackhole: Blackhole) {
        blackhole.consume(ComponentWithDigits.digits())
    }
}

private val scopeWithNumber = scope("") {
    element<Int> { 42 }
}

private val scopeWithDigits = scope("") {
    element<Int> { 0 }
    element<Int> { 1 }
    element<Int> { 2 }
    element<Int> { 3 }
    element<Int> { 4 }
    element<Int> { 5 }
    element<Int> { 6 }
    element<Int> { 7 }
    element<Int> { 8 }
    element<Int> { 9 }
}

private object ComponentWithNumber : Component(scopeWithNumber) {
    fun number() = collect<Int>()
}

private object ComponentWithDigits : Component(scopeWithDigits) {
    fun digits() = collect<Int>()
}
