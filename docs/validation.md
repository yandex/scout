# Validation

Scout provides configurable dependency graph validator. You can use built-in checks or 
implement your own. It's recommended to write unit-test for dependency validations.

Use `Validator#configure` method to set up validation process. Recommended way to use 
`Validator` is to write unit-test with `Validator#validate` call. This method throws error 
with details about dependency graph problems. Otherwise you can use 
`Validator#validateWithResult` to analyse the result manually. 

```kotlin
class DependencyGraphTest {
    @Test
    fun `Check that dependency graph passes all checks`() {
        Validator.configure()
            .withConsistencyCheck()
            .withOverridesCheck()
            .withScopeNamingCheck { name -> name.isNotEmpty() }
            .withCustomCheck(MyRuleChecker())
            .validate(ComponentCollector())
    }
}
```

```
Validate 1 components using ConsistencyChecker, OverridesChecker, ScopeNamingChecker, 
MyRuleChecker
 ✔ Consistency check passed without errors
 ✔ Overrides check passed without errors
 ✔ Scope naming check passed without errors
 ✔ My rule check passed without errors
```

## Setup

```groovy
// Gradle: build.gradle
dependencies {
    testImplementation "com.yandex.scout:scout-validator:$scout_version" // required: 
validator and built-in checkers
    testImplementation "com.yandex.scout:classgraph-collector:$scout_version" // optional: 
built-in component collector
}
```

It's recommended to use built-in `ComponentCollector`, but you can make custom 
`ComponentProducer` implementation.

## Component collector

Scout provides default implementation of `ComponentProducer` interface based on [Class Graph 
library](https://github.com/classgraph/classgraph). Collector can be created by default 
constructor but it's recommended to configure collecting process.

```kotlin
val componentProducer = ComponentCollector(
    // filter sources by root package names
    packageNames = listOf("my.package1", "other.package2"),
    // filter classpaths for scanning
    classpathFilter = { classpath -> classpath.endsWith(".jar") && 
!classpath.endsWith("/res.jar") },
    // set custom class loader
    classLoader = InstrumentationRegistry.getArguments().classLoader,
    // custom instance producer (ReflectiveInstanceProducer is a part of 
    instanceProducer = ReflectiveInstanceProducer { _, _, parameterType ->
        when (parameterType) {
            Fragment::class.java -> fragmentMock
            View::class.java -> viewMock
            else -> Mockito.mock(parameterType)
        }
    }
)
```

## Consistency check

Checks that each component method (corresponding to the filter) completes without errors and 
deferred requests (lazies and providers) completes without errors too. Methods invokes using 
passed invoker so you can customize invoke logic (for example, argument stubbing).

```kotlin
// basic
Validator.configure()
    .withConsistencyCheck()
    .validate(componentProducer)

// customized
Validator.configure()
    .withConsistencyCheck(
        methodFilter = { /* some method filtering logic */ },
        methodInvoker = { receiver, method -> /* some invokation logic */ }
    )
    .validate(componentProducer)
```

Consistency check implements by class: `ConsistencyChecker`.

## Overrides check

Checks that scope of each component does not contain illegal object factory overrides. In 
terms of this class, "illegal override" means parent scope has object factory for type and 
child scope has factory for this type without "allowOverride" flag raise.

```kotlin
Validator.configure()
    .withOverridesOverridesCheck()
    .validate(componentProducer)
```

Overrides check implements by class: `OverridesChecker`.

## Scope naming check

Check that scope name of each component satisfies passed rule.

```kotlin
Validator.configure()
    .withScopeNamingChecker { name -> name.isNotEmpty() }
    .validate(componentProducer)
```

Scope naming check implements by class: `ScopeNamingChecker`.

