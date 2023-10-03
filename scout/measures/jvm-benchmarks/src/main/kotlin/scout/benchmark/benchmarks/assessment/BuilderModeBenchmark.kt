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
import scout.Scope
import scout.Scout
import scout.benchmark.DEFAULT_FORKS
import scout.benchmark.DEFAULT_MEASURE_ITERATIONS
import scout.benchmark.DEFAULT_MEASURE_SECONDS
import scout.benchmark.DEFAULT_WARMUP_ITERATIONS
import scout.benchmark.DEFAULT_WARMUP_SECONDS
import scout.benchmark.Optimized
import scout.scope
import scout.scope.builder.ScopeBuilder
import java.util.concurrent.TimeUnit

@Fork(DEFAULT_FORKS)
@Warmup(iterations = DEFAULT_WARMUP_ITERATIONS, time = DEFAULT_WARMUP_SECONDS)
@Measurement(iterations = DEFAULT_MEASURE_ITERATIONS, time = DEFAULT_MEASURE_SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class BuilderModeBenchmark : Optimized() {

    @Benchmark
    fun unsafe(blackhole: Blackhole) {
        blackhole.consume(buildScope(ScopeBuilder.ThreadSafetyMode.Unsafe))
    }

    @Benchmark
    fun confined(blackhole: Blackhole) {
        blackhole.consume(buildScope(ScopeBuilder.ThreadSafetyMode.Confined))
    }

    @Benchmark
    fun synchro(blackhole: Blackhole) {
        blackhole.consume(buildScope(ScopeBuilder.ThreadSafetyMode.Synchronized))
    }

    private fun buildScope(mode: ScopeBuilder.ThreadSafetyMode): Scope {
        Scout.ThreadSafety.setDefaultScopeBuilderMode(mode)
        return scope("") {
            factory<Type0> { Type0 }
            factory<Type1> { Type1 }
            factory<Type2> { Type2 }
            factory<Type3> { Type3 }
            factory<Type4> { Type4 }
        }
    }

    private object Type0
    private object Type1
    private object Type2
    private object Type3
    private object Type4
}
