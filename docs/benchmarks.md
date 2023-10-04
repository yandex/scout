# Benchmarks

The repository contains [JMH-benchmarks](https://github.com/openjdk/jmh) and a platform for running them. 
Existing benchmarks splits into [assessments](https://github.com/yandex/scout/tree/main/scout/measures/jvm-benchmarks/src/main/kotlin/scout/benchmark/benchmarks/assessment) and [comparisons](https://github.com/yandex/scout/tree/main/scout/measures/jvm-benchmarks/src/main/kotlin/scout/benchmark/benchmarks/comparison). 
You can find it in [jvm-benchmarks module](https://github.com/yandex/scout/tree/main/scout/measures/jvm-benchmarks). 

## Launcher
You can run benchmarks using `main` method in [jvm-benchmarks/Launcher.kt](https://github.com/yandex/scout/blob/main/scout/measures/jvm-benchmarks/src/main/kotlin/scout/benchmark/Launcher.kt). 
Once you have launched benchmarks, you will see run-configurator. Enter numbers of benchmarks you want to run. It's possible to specify several benchmark numbers separated by space. Enter `0` to run all existing benchmarks.
```
Assessment benchmarks:
1. get call
2. collect call
3. associate call
...

Comparison benchmarks:
7. get constant
8. get / warm / all
9. get / warm / small
...

Enter benchmark numbers: 1 2 3
```

When the run is completed, the launcher will show you the results and a comparison with control results:
```
Benchmark                                            Control       Test       Diff     Conclusion
AssociateCallBenchmark.associationWith10Entries      183.394    156.367     -14.7%         (GOOD)
AssociateCallBenchmark.associationWith1Entry          15.562     16.467      +5.8%          (BAD)
CollectCallBenchmark.listWith10Elements               64.420     67.985      +5.5%          (BAD)
CollectCallBenchmark.listWith1Element                 10.442     10.823      +3.6%               
GetCallBenchmark.factory                               2.865      3.217     +12.3%          (BAD)
GetCallBenchmark.reusable                              4.292      4.622      +7.7%          (BAD)
GetCallBenchmark.singleton                             3.703      3.729      +0.7%               

Do you want to rewrite control results? (Y)es/(n)o: 
```

Enter `y`, `Y`, `yes` to save the results as control results if you want.

## Scenarios
Once you have implemented new benchmark, you should to register new scenario for launcher:
```kotlin
// jvm-benchmarks: Launcher.kt
private val assessments = listOf(
    ...
    "my new benchmark" includes listOf(
        MyNewBenchmark::class
    )
)
```
From now "my new benchmark" option will be available in run-configurator.

## Accuracy
To make your results more accurate, increase benchmark settings constants:
```kotlin
// jvm-benchmarks: Launcher.kt
const val DEFAULT_WARMUP_ITERATIONS = 1 // -> 2
const val DEFAULT_WARMUP_SECONDS = 1 // -> 3
const val DEFAULT_MEASURE_ITERATIONS = 1 // -> 5
const val DEFAULT_MEASURE_SECONDS = 1 // -> 3
const val DEFAULT_FORKS = 1 // -> 3
const val COLD_MEASURE_FORKS = 10 // -> 30
```
Further runs will take longer but more accurate, be patient.

## Test data
In order to run benchmarks, test data is needed. To simplify test data inflation, we have implemented [graph-generator-plugin](https://github.com/yandex/scout/tree/main/scout/measures/graph-generator-plugin). You can add some test graphs:
```groovy
graphs {
    generate("g5nodes").nodes(5).modules(1, 3)
    generate("g25nodes").nodes(25).modules(5, 10)
    generate("g125nodes").nodes(125).modules(15, 50)
    // add custom graph configuration here
    // method 'nodes' specifies number of nodes in graph
    // method 'roots' specifies number of roots in graph
    // method 'modules' specifies minimal and maximal module size
    // method 'random' can be use to make generation random (determined by default)
}
```
New graph will be generated while benchmarks build process (sources will be populated to `jvm-benchmarks/build/generated/sources/graphGenerator/graphs` folder).
