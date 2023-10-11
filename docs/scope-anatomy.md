# Scope Anatomy
Scope is the main concept of the library. It represents an isolated dependency graph – a set of dependency creation rules and instances created using these rules. Scope can contain an arbitrary number of factories and depend on an arbitrary number of other scopes.

## Scope creation
Scope can be created using higher-order function `scope` from `com.yandex.scout:scout-core:*`. The first function parameter is the scope name (greatly simplifies debugging). The last function parameter is lambda with scope content declaration (factories and parent scopes).

```kotlin
val appScope = scope("app-scope") {
    dependsOn(coreScope)
    factory<SomeFeature> {
        ..
    }
}
```

## Scope factories
Factories can be registered in scope using `factory`, `reusable`, `singleton`, `element` and `mapping` functions. 

```kotlin
val appScope = scope("app-scope") {
    factory<SomeFeature> { .. }
    reusable<SomeFeatureMapper> { .. }
    singleton<SomeFeatureStore> { .. }
}
```

Registered factories holds by `Registry` type. For better code structure, it is recommended to declare factories in a separate extension methods for the `Registry` type.

```kotlin
val appScope = scope("app-scope") {
    useSomeFeatureBeans()
    ..
}

fun Registry.useSomeFeatureBeans() {
    factory<SomeFeature> { .. }
    reusable<SomeFeatureMapper> { .. }
    singleton<SomeFeatureStore> { .. }
}
```

## Scope parents

Modern applications can be quite large. They are often divided into modules. Is it bad idea to store all dependencies is a single scope. Scope can depend on other scopes (use their factories to create your dependencies).

```kotlin
val coreScope = scope("core-scope") {
    reusable<DateFormatter> { .. }
}

val appScope = scope("app-scope") {
    dependsOn(coreScope)
    factory<SomeFeature> {
        SomeFeature(dateFormatter = get())
    }
}
```

## Thread safety

Scope formation can be performed in various thread-safety modes:
- **Thread Unsafe (Default).** Factories and parents registers without any synchronizations. This is the most performant mode (should suit most applications).
- **Thread Confined.** In this mode each method invocation will be checked to be performed by exact same thread. Exception will be thrown otherwise. *Dramatically slows down performance, so don't use this mode for release builds.*
- **Synchronized.** Each scope builder method invocation will be synchronized internally.

```kotlin
val appScope = scope("app-scope", ScopeBuilder.ThreadSafetyMode.Synchronized) {
    ..
}
```

You can set default thread safety mode globally using `Scout` object.

```kotlin
Scout.ThreadSafety.setDefaultScopeBuilderMode(ScopeBuilder.ThreadSafetyMode.Synchronized)
```

To make sure that all factories are registered on the same thread, use **Confined** mode for debug builds.

```kotlin
Scout.ThreadSafety.setDefaultScopeBuilderMode(
    if (BuildConfig.DEBUG) {
        ScopeBuilder.ThreadSafetyMode.Confined
    } else {
        ScopeBuilder.ThreadSafetyMode.Unsafe
    }
)
```
