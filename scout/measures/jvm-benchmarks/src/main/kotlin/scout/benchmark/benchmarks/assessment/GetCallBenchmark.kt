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
open class GetCallBenchmark : Optimized() {

    @Benchmark
    fun factory(blackhole: Blackhole) {
        blackhole.consume(ComponentWithFactory.number())
    }

    @Benchmark
    fun singleton(blackhole: Blackhole) {
        blackhole.consume(ComponentWithSingleton.number())
    }

    @Benchmark
    fun reusable(blackhole: Blackhole) {
        blackhole.consume(ComponentWithReusable.number())
    }
}

private val scopeWithFactory = scope("") {
    factory<Int> { 42 }
}

private val scopeWithSingleton = scope("") {
    singleton<Int> { 42 }
}

private val scopeWithReusable = scope("") {
    reusable<Int> { 42 }
}

private object ComponentWithFactory : Component(scopeWithFactory) {
    fun number(): Int = get()
}

private object ComponentWithSingleton : Component(scopeWithSingleton) {
    fun number(): Int = get()
}

private object ComponentWithReusable : Component(scopeWithReusable) {
    fun number(): Int = get()
}
