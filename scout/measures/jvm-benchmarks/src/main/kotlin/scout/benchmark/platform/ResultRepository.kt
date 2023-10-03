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
import java.io.File

private const val RESULT_DIR_PATH = "measures/jvm-benchmarks/results"
private const val RESULT_FILE_PATH = "$RESULT_DIR_PATH/control.csv"

internal fun fetchControlResults(): Map<String, Double> {
    val file = File(RESULT_FILE_PATH)
    if (file.exists()) {
        val lines = file.readLines()
        return lines.associate { line ->
            val (label, score) = line.split(",", ", ")
            label to score.toDouble()
        }
    }
    return emptyMap()
}

internal fun saveResults(results: Collection<RunResult>) {
    val formatted = formatResults(results)
    File(RESULT_DIR_PATH).mkdirs()
    File(RESULT_FILE_PATH).apply {
        createNewFile()
        writeText(formatted)
    }
    println()
    println("Control results was updated!")
}
