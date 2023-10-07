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
package scout.benchmark

import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder
import scout.Scout
import scout.benchmark.benchmarks.assessment.AssociateCallBenchmark
import scout.benchmark.benchmarks.assessment.BuilderModeBenchmark
import scout.benchmark.benchmarks.assessment.CollectCallBenchmark
import scout.benchmark.benchmarks.assessment.GetCallBenchmark
import scout.benchmark.benchmarks.assessment.InitScopeBenchmark
import scout.benchmark.benchmarks.assessment.ParentAccessBenchmark
import scout.benchmark.benchmarks.comparison.ColdGet125Benchmark
import scout.benchmark.benchmarks.comparison.ColdGet25Benchmark
import scout.benchmark.benchmarks.comparison.ColdGet5Benchmark
import scout.benchmark.benchmarks.comparison.GetConstantBenchmark
import scout.benchmark.benchmarks.comparison.GraphInit125Benchmark
import scout.benchmark.benchmarks.comparison.GraphInit25Benchmark
import scout.benchmark.benchmarks.comparison.GraphInit5Benchmark
import scout.benchmark.benchmarks.comparison.WarmGet125Benchmark
import scout.benchmark.benchmarks.comparison.WarmGet25Benchmark
import scout.benchmark.benchmarks.comparison.WarmGet5Benchmark
import scout.benchmark.platform.Scenario
import scout.benchmark.platform.compareResults
import scout.benchmark.platform.saveResults
import java.lang.IllegalArgumentException
import java.util.Scanner
import kotlin.reflect.KClass

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        interactive()
    } else {
        independent(args)
    }
}

const val DEFAULT_WARMUP_ITERATIONS = 1
const val DEFAULT_WARMUP_SECONDS = 1
const val DEFAULT_MEASURE_ITERATIONS = 1
const val DEFAULT_MEASURE_SECONDS = 1
const val DEFAULT_FORKS = 1
const val COLD_MEASURE_FORKS = 10

private val assessments = listOf(
    "get call" includes listOf(
        GetCallBenchmark::class
    ),
    "collect call" includes listOf(
        CollectCallBenchmark::class
    ),
    "associate call" includes listOf(
        AssociateCallBenchmark::class
    ),
    "parent access" includes listOf(
        ParentAccessBenchmark::class
    ),
    "init scope" includes listOf(
        InitScopeBenchmark::class
    ),
    "builder mode" includes listOf(
        BuilderModeBenchmark::class
    )
)

private val comparisons = listOf(
    "get constant" includes listOf(
        GetConstantBenchmark::class
    ),
    "get / warm / all" includes listOf(
        WarmGet5Benchmark::class,
        WarmGet25Benchmark::class,
        WarmGet125Benchmark::class,
    ),
    "get / warm / small" includes listOf(
        WarmGet5Benchmark::class
    ),
    "get / warm / medium" includes listOf(
        WarmGet25Benchmark::class
    ),
    "get / warm / large" includes listOf(
        WarmGet125Benchmark::class
    ),
    "get / cold / all" includes listOf(
        ColdGet5Benchmark::class,
        ColdGet25Benchmark::class,
        ColdGet125Benchmark::class,
    ),
    "get / cold / small" includes listOf(
        ColdGet5Benchmark::class
    ),
    "get / cold / medium" includes listOf(
        ColdGet25Benchmark::class
    ),
    "get / cold / large" includes listOf(
        ColdGet125Benchmark::class
    ),
    "graph init / all" includes listOf(
        GraphInit5Benchmark::class,
        GraphInit25Benchmark::class,
        GraphInit125Benchmark::class,
    ),
    "graph init / small" includes listOf(
        GraphInit5Benchmark::class
    ),
    "graph init / medium" includes listOf(
        GraphInit25Benchmark::class
    ),
    "graph init / large" includes listOf(
        GraphInit125Benchmark::class
    ),
)

abstract class Optimized {

    @Setup
    fun setup() {
        Scout.Optimizations.disableInterceptors()
    }
}

private val options = OptionsBuilder()
    .mode(Mode.AverageTime)

private fun interactive() {
    val scanner = Scanner(System.`in`)

    println("Assessment benchmarks:")
    var index = 1
    for (scenario in assessments) {
        println("$index. ${scenario.name}")
        index += 1
    }
    println()
    println("Comparison benchmarks:")
    for (scenario in comparisons) {
        println("$index. ${scenario.name}")
        index += 1
    }

    print("\nEnter benchmark numbers: ")

    val benchmarks = selectBenchmarks(
        answer = scanner.nextLine()
    ) ?: return

    val results = Runner(options + benchmarks).run()

    compareResults(results)

    print("\nDo you want to rewrite control results? (Y)es/(n)o: ")

    if (isPositiveAnswer(scanner.nextLine())) {
        saveResults(results)
    }
}

private fun independent(args: Array<String>) {
    val indices = args.map { arg -> arg.toInt() }
    val scenarios = mutableListOf<Scenario>()
    for (index in indices) {
        if (index < 0) {
            throw IllegalArgumentException("Scenario out of bounds: $index")
        } else if (index < assessments.size) {
            scenarios += assessments[index]
        } else if (index < assessments.size + comparisons.size) {
            scenarios += comparisons[index - assessments.size]
        } else {
            throw IllegalArgumentException("Scenario out of bounds: $index")
        }
    }

    val benchmarks = selectBenchmarks(
        indices = indices
    ) ?: return

    val results = Runner(options + benchmarks).run()

    compareResults(results)

    saveResults(results)
}

private fun selectBenchmarks(answer: String): List<KClass<*>>? {
    val indices = try {
        answer.split(" ", ",", ";").map { index -> index.toInt() }
    } catch (e: Throwable) {
        println("Expected numeric answer, get \"$answer\"")
        return null
    }
    return selectBenchmarks(indices)
}

private fun selectBenchmarks(indices: List<Int>): List<KClass<*>>? {
    val benchmarks = mutableListOf<KClass<*>>()
    for (index in indices) {
        if (index == 0) {
            return (assessments + comparisons).map { scenario ->
                scenario.benchmarks
            }.flatten().distinct()
        } else if (0 < index && index <= assessments.size) {
            benchmarks.addAll(assessments[index - 1].benchmarks)
        } else if (assessments.size < index && index <= assessments.size + comparisons.size) {
            benchmarks.addAll(comparisons[index - assessments.size - 1].benchmarks)
        } else {
            println("Scenario number is out of range: $index")
            return null
        }
    }
    return benchmarks.distinct()
}

private fun isPositiveAnswer(answer: String): Boolean {
    return answer.lowercase() in setOf("y", "yes")
}

private infix fun String.includes(benchmarks: List<KClass<*>>): Scenario {
    return Scenario(this, benchmarks)
}

private operator fun ChainedOptionsBuilder.plus(benchmarks: List<KClass<*>>): Options {
    return apply {
        benchmarks.forEach { benchmark ->
            include(benchmark.java.simpleName)
        }
    }.build()
}
