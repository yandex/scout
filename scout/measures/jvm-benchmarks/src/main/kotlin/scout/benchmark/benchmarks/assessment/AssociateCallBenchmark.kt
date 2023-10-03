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
open class AssociateCallBenchmark : Optimized() {

    @Benchmark
    fun associationWith1Entry(blackhole: Blackhole) {
        blackhole.consume(ComponentWithAxiom.axiom())
    }

    @Benchmark
    fun associationWith10Entries(blackhole: Blackhole) {
        blackhole.consume(ComponentWithNegatives.negatives())
    }
}

private val scopeWithAxiom = scope("") {
    mapping<Int, Int> { 42 to 42 }
}

private val scopeWithNegatives = scope("") {
    mapping<Int, Int> { 0 to -0 }
    mapping<Int, Int> { 1 to -1 }
    mapping<Int, Int> { 2 to -2 }
    mapping<Int, Int> { 3 to -3 }
    mapping<Int, Int> { 4 to -4 }
    mapping<Int, Int> { 5 to -5 }
    mapping<Int, Int> { 6 to -6 }
    mapping<Int, Int> { 7 to -7 }
    mapping<Int, Int> { 8 to -8 }
    mapping<Int, Int> { 9 to -9 }
}

private object ComponentWithAxiom : Component(scopeWithAxiom) {
    fun axiom() = associate<Int, Int>()
}

private object ComponentWithNegatives : Component(scopeWithNegatives) {
    fun negatives() = associate<Int, Int>()
}