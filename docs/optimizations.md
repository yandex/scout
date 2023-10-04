# Optimizations
This manual describes build-in optimizations that you can enable if it's necessary.

## Disable Interceptors
Scout provides the ability to explicilty disable `Interceptor`s mechanism. This option can gain up to 10% dependency resolving speedup. Just call `disableInterceptors` method on program start.
```kotlin
Scout.Optimizations.disableInterceptors()
```

```
Benchmark              Control       Test       Diff     Conclusion
WarmGet5.scout          26.209     23.839      -9.0%         (GOOD)
WarmGet25.scout        125.585    119.040      -5.2%         (GOOD)
WarmGet125.scout       656.996    610.224      -7.1%         (GOOD)
```

## Switch to Compiled Keys
Scout provides version with compiled (integer) keys. This version reduces graph creation time up to 50%, but a bit slows dependency resolving. Key compilation carried with `scout-gradle-plugin`.

**1. Add "-with-compiled-keys" suffix to core, inspector and validator dependencies**
```groovy
// before
dependencies {
    implementation 'com.yandex.scout:scout-core:$scout_version'
    testImplementation 'com.yandex.scout:scout-inspector:$scout_version'
    testImplementation 'com.yandex.scout:scout-validator:$scout_version'
}

// after
dependencies {
    implementation 'com.yandex.scout:scout-core-with-compiled-keys:$scout_version'
    testImplementation 'com.yandex.scout:scout-inspector-with-compiled-keys:$scout_version'
    testImplementation 'com.yandex.scout:scout-validator-with-compiled-keys:$scout_version'
}
```

**2. Add plugin classpath**
```groovy
// root: build.gradle
buildscript {
    dependencies {
        classpath 'com.yandex.scout:scout-gradle-plugin:$scout_version'
    }
}
```

**3. Apply plugin**
```groovy
// root: build.gradle
plugins {
    ...
}

apply plugin: 'scout-gradle-plugin'
```

**4. Configure plugin**
```groovy
apply plugin: 'scout-gradle-plugin'

scout {
    compileKeys = true // enable key compilation
    generateCompiledKeyMapper = true // enable mapper generation (to decode compiled keys in stacktraces)
}
```

**5. Enable key decoding**
```kotlin
Scout.enableKeyDecoding()
```

```
Benchmark           Mode  Cnt    Score     Error  Units
Init5000.scout(int) avgt   20   591.009 ± 300.563  ms/op
Init5000.scout(cls) avgt   20  1010.567 ± 298.631  ms/op
```
