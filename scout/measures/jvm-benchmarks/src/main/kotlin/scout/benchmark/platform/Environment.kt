package scout.benchmark.platform

internal object Environment {
    const val RESULT_DIR_PATH = "measures/jvm-benchmarks/results"
    const val RESULT_FILE_PATH = "$RESULT_DIR_PATH/control.csv"
    const val COMPARE_FILE_PATH = "$RESULT_DIR_PATH/compare.txt"
}