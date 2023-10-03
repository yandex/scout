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

import graphs.g25nodes.dagger.daggerInitGraph
import graphs.g25nodes.katana.katanaInitGraph
import graphs.g25nodes.kodein.kodeinInitGraph
import graphs.g25nodes.koin.koinInitGraph
import graphs.g25nodes.scout.scoutInitGraph
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import scout.benchmark.COLD_MEASURE_FORKS
import scout.benchmark.Optimized
import java.util.concurrent.TimeUnit

@Fork(COLD_MEASURE_FORKS)
@Warmup(iterations = 0)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class GraphInit25Benchmark : Optimized() {

    @Benchmark
    fun scout(blackhole: Blackhole) {
        blackhole.consume(scoutInitGraph())
    }

    @Benchmark
    fun koin(blackhole: Blackhole) {
        blackhole.consume(koinInitGraph())
    }

    @Benchmark
    fun kodein(blackhole: Blackhole) {
        blackhole.consume(kodeinInitGraph())
    }

    @Benchmark
    fun katana(blackhole: Blackhole) {
        blackhole.consume(katanaInitGraph())
    }

    @Benchmark
    fun dagger(blackhole: Blackhole) {
        blackhole.consume(daggerInitGraph())
    }
}
