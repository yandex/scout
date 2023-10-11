# Essential Rules

This section provides a set of rules that must be followed in order for your graph to be 
validated. Violation of these rules will lead to validation gaps and possible problems in 
production.

## Scope should be determined
The component defines an access interface to the dependency graph while the scope determines 
the contents of the dependency graph. There is no sense to validate component if scope is not 
determined. If a scope passes to a component from the outside, the test cannot guarantee that 
the same scope will be passed during program execution.

```kotlin
class GoodComponent : Component(myScope) { ... }
class BadComponent(scope: Scope) : Component(scope)
```

## Request should be determined

Component methods should not branch with a choice of the type to be queried. If a method does 
not determine the requested type, the test cannot guarantee that the same type will be 
requested during program execution.

```kotlin
class GoodComponent : Component(myScope) {
    fun getMyController(): MyController = get()    
    fun getMyService(): MyService = get()
}

class BadComponent : Component(myScope) {
    fun getMyControllerOrService() = if (Random.nextBoolean()) {
        get<MyController>()
    } else {
        get<MyService>()
    }
}
```
## Request should be synchronous

Component methods must synchronously query all necessary types from the graph. If there are 
asynchronous calls in a component method, they will not be validated because they will not be 
called in the test. Сomponent class provides special methods for obtaining `Lazy` and 
`Provider`.

```kotlin
class GoodComponent : Component(myScope) {
    fun getMyController(): MyController = get()
    fun getMyControllerLazy(): Lazy<MyController> = getLazy()
}

class BadComponent : Component(myScope) {
    fun getMyController() = lazy {
        // oops! get() fails on lazy.value call
        get<MyComponent>()
    }
}
```

## Factory should be determined

The definition of how an object is created by a factory should be without branches. If the 
factory contains branches, the test cannot guarantee that the same branch of the factory code 
will be executed while the program is running.

```kotlin
factory<MyController> { // good factory
    MyController(get())
}

singleton<MyService> { // bad factory
    if (BuildConfig.DEBUG) {
        MyDebugServiceImpl()
    } else {
        // oops! get() fails in release build
        MyReleaseServiceImpl(get())
    }
}
```

## Factory should be synchronous

The factory must synchronously receive all dependencies to create an object. If the factory 
has `get` / `collect` / `associate` calls in asynchronous blocks, those calls will not be 
validated. Special methods are available in the factory to obtain `Lazy` / `Provider`.

```kotlin
factory<MyService> { // good factory
    val settings = getLazy<ServerSettings>()
    MyService(
        hostProvider = { settings.getHost() }
    )
}

factory<MyService> { // bad factory
    MyService(
        hostProvider = { 
            // oops! get() fails with first provider call
            get<ServerSettings>().getHost()
        }
    )
}
```

## Factory should be defined undoubtedly

The factory must be registered without any conditions. If the factory registering depends on some condition, the test cannot guarantee that graph is consistent for both: when the condition is met and when it is not met.

```kotlin
if (BuildConfig.DEBUG) { // bad idea
    factory<DebugFeatureManager> {
        DebugFeatureManager()
    }
}

factory<DebugFeatureManager> { // good idea
    ProductionDebugFeatureManager()
}

Scout.Interceptors.register( // replace factory for debug
    ObjectFactoryReplacer {
        factory<DebugFeatureManager> {
            DevelopmentDebugFeatureManager()
        }
    }
}
```
