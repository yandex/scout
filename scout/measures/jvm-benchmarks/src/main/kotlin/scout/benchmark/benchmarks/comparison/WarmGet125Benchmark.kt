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
package scout.benchmark.benchmarks.comparison

import graphs.g125nodes.dagger.daggerGetNode0
import graphs.g125nodes.katana.katanaGetNode0
import graphs.g125nodes.kodein.kodeinGetNode0
import graphs.g125nodes.koin.koinGetNode0
import graphs.g125nodes.scout.scoutGetNode0
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import scout.benchmark.DEFAULT_FORKS
import scout.benchmark.DEFAULT_MEASURE_ITERATIONS
import scout.benchmark.DEFAULT_MEASURE_SECONDS
import scout.benchmark.DEFAULT_WARMUP_ITERATIONS
import scout.benchmark.DEFAULT_WARMUP_SECONDS
import scout.benchmark.Optimized
import java.util.concurrent.TimeUnit

@Fork(DEFAULT_FORKS)
@Warmup(iterations = DEFAULT_WARMUP_ITERATIONS, time = DEFAULT_WARMUP_SECONDS)
@Measurement(iterations = DEFAULT_MEASURE_ITERATIONS, time = DEFAULT_MEASURE_SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class WarmGet125Benchmark : Optimized() {

    @Benchmark
    fun scout(blackhole: Blackhole) {
        blackhole.consume(scoutGetNode0())
    }

    @Benchmark
    fun koin(blackhole: Blackhole) {
        blackhole.consume(koinGetNode0())
    }

    @Benchmark
    fun kodein(blackhole: Blackhole) {
        blackhole.consume(kodeinGetNode0())
    }

    @Benchmark
    fun katana(blackhole: Blackhole) {
        blackhole.consume(katanaGetNode0())
    }

    @Benchmark
    fun dagger(blackhole: Blackhole) {
        blackhole.consume(daggerGetNode0())
    }
}
