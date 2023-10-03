# Deferred requests

Scout supports out of the box `Lazy` and `Provider` wrappers for dependency requests. Each 
regular accessor method has `Lazy` and `Provider` alternatives, so you can defer heavy object 
initialization.

## Lazy
There is no any specific declaration for dependency to make it accessible as `Lazy`:
```kotlin
class AppController(
    private val awesomeFeature: Lazy<AwesomeFeature>,
    private val excellentFeature: Lazy<ExcellentFeature>,
    private val incredibleFeature: Lazy<IncredibleFeature>
)

val appScope = scope("app-scope") {
    factory<AwesomeFeature> { AwesomeFeature() }
    factory<ExcellentFeature> { ExcellentFeature() }
    factory<IncredibleFeature> { IncredibleFeature() }
    factory<AppController> {
        AppController(
            awesomeFeature = getLazy(),
            excellentFeature = getLazy(),
            incredibleFeature = getLazy()
        )
    }
}

object AppScope : Component(coffeeMakerScope) {
    fun controller() = getLazy<AppController>()
}
```

There are also `optLazy`, `collectLazy` and `associateLazy` methods available inside factory 
block.

## Provider
There is no any specific declaration for dependency to make it accessible as `Provider`:
```kotlin
class AppController(
    private val awesomeFeature: Provider<AwesomeFeature>,
    private val excellentFeature: Provider<ExcellentFeature>,
    private val incredibleFeature: Provider<IncredibleFeature>
)

val appScope = scope("app-scope") {
    factory<AwesomeFeature> { AwesomeFeature() }
    factory<ExcellentFeature> { ExcellentFeature() }
    factory<IncredibleFeature> { IncredibleFeature() }
    factory<AppController> {
        AppController(
            awesomeFeature = getProvider(),
            excellentFeature = getProvider(),
            incredibleFeature = getProvider()
        )
    }
}

object AppScope : Component(coffeeMakerScope) {
    fun controller() = getProvider<AppController>()
}
```

There are also `optProvider`, `collectProvider` and `associateProvider` methods available 
inside factory block.

