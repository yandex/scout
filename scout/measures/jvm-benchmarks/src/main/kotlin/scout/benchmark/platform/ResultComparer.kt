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
package scout.benchmark.platform

import org.openjdk.jmh.results.RunResult

internal fun compareResults(results: Collection<RunResult>) {
    val current = transformRunResults(results)
    val control = fetchControlResults()
    val labels = control.keys + current.keys
    val longest = labels.maxOfOrNull { label -> label.length } ?: 0
    val format = "%-${longest + 2}s %10s %10s %10s %14s"
    println()
    println(String.format(format, "Benchmark", "Control", "Test", "Diff", "Conclusion"))
    for (label in control.keys + current.keys) {
        val controlScore = control[label]
        val currentScore = current[label]
        val (difference, conclusion) = when {
            controlScore == null && currentScore == null -> "-" to ""
            controlScore == null -> "-" to "new"
            currentScore == null -> "-" to "missing"
            else -> {
                val diff = (currentScore - controlScore) / controlScore * 100
                if (diff > 0) {
                    val suffix = if (diff > 5.0) " (BAD)" else ""
                    "+${String.format("%.1f", diff)}%" to suffix
                } else {
                    val suffix = if (diff < -5.0) " (GOOD)" else ""
                    "${String.format("%.1f", diff)}%" to suffix
                }
            }
        }
        println(
            String.format(
                format,
                label,
                String.format("%.3f", controlScore),
                String.format("%.3f", currentScore),
                difference,
                conclusion
            )
        )
    }
}

private fun transformRunResults(results: Collection<RunResult>): Map<String, Double> {
    return results.associate { result ->
        formatBenchmarkId(result) to getBenchmarkScore(result)
    }
}
