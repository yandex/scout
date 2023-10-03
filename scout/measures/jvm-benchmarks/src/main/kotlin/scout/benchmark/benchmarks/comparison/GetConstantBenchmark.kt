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

import dagger.Component
import dagger.Provides
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.direct
import org.kodein.di.instance
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import org.rewedigital.katana.Module
import org.rewedigital.katana.dsl.factory
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
open class GetConstantBenchmark : Optimized() {

    @Benchmark
    fun scout(blackhole: Blackhole) {
        blackhole.consume(ScoutComponent.int())
    }

    @Benchmark
    fun koin(blackhole: Blackhole) {
        blackhole.consume(koin.get<Int>())
    }

    @Benchmark
    fun kodein(blackhole: Blackhole) {
        blackhole.consume(kodein.instance<Int>())
    }

    @Benchmark
    fun katana(blackhole: Blackhole) {
        blackhole.consume(katana.custom<Int>())
    }

    @Benchmark
    fun dagger(blackhole: Blackhole) {
        blackhole.consume(dagger.integer())
    }
}

private val scoutScope = scope("") {
    factory<Int> { 42 }
}

private object ScoutComponent : scout.Component(scoutScope) {
    fun int(): Int = get()
}

private val koin = startKoin {
    modules(module {
        factory { 42 }
    })
}.koin

private val kodein = DI {
    import(
        DI.Module("") {
            bindProvider { 42 }
        }
    )
}.direct

private val katana = org.rewedigital.katana.Component(
    Module {
        factory { 42 }
    }
)

@dagger.Module
internal class DaggerModule {
    @Provides
    fun provideInt() = 42
}

@Component(modules = [DaggerModule::class])
private interface DaggerComponent {
    fun integer(): Int
}

private val dagger = DaggerDaggerComponent.create()
