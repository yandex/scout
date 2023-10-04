<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./docs/assets/scout-logo-dark.png">
  <img src="./docs/assets/scout-logo-light.png" alt="Swift logo" height="70">
</picture>

<div><br></div>

[![Maven Central][mavenbadge-svg]][mavencentral]

A fast and safe manual dependency injector for Kotlin and Android.

Scout is a runtime dependency injection library. It provides excellent Domain Specific 
Language for factory declarations and configurable dependency graph validator. The dependency 
graph resolves it's nodes really fast despite the runtime approach.

Scout is actively maintained by Yandex. The library is currently in beta, as we want to get 
feedback from users and to test a couple of promissing ideas. The library API is quite stable, 
but we do not exclude the possibility that we will make minor changes to it.

## Usage

Actually Scout is a dependency container, so let's fill that container with some definitions. 
Scope is responsible for storing definitions and created instances.
```kotlin
val appScope = scope("app-scope") {
    singleton<BooksDataStore> {
        BooksDataStore()
    }
    reusable<BookMapper> {
        BookMapper()
    }
    factory<BooksRepository> {
        BooksRepository(store = get(), mapper = get())
    }
}
```

Scope prohibits random access to it's content. The only way to get some instance from scope is 
to declare component.
```kotlin
object AppComponent : Component(appScope) {
    fun repository(): BooksRepository = get()
}
```

As soon as component is instantiated we are able to access our dependency graph.
```kotlin
fun main() = AppComponent
    .repository()
    .sync()
```

Our program is ready, let's test the dependency graph.
```kotlin
class DependencyGraphTest {
    @Test
    fun `Check dependency graph is consistent`() {
        Validator.configure()
            .withConsistencyCheck()
            .validate(ComponentCollector())
    }
}
```

Now every new component in our program will be automatically collected for testing.
```
Validate 1 components using ConsistencyChecker
 ✔ Consistency check passed without errors
```

## Setup
```groovy
// Gradle: build.gradle
dependencies {
    implementation "com.yandex.scout:scout-core:$scout_version" // main library
    testImplementation "com.yandex.scout:scout-validator:$scout_version" // validator and built-in checks
    testImplementation "com.yandex.scout:classgraph-collector:$scout_version" // built-in component collector
}
```

## Tutorials

You can find here tutorials to help you learn and get started with Scout:
- [Quick Start](docs/quick-start-guide.md)
- [Essential Rules](docs/essential-rules.md)
- [Validation Guide](docs/validation.md)
- [Multibindings](docs/multibindings.md)
- [Lazy and Provider](docs/deferred-requests.md)
- [Benchmarks](docs/benchmarks.md)

## License
```
Copyright 2023 Yandex LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
ou may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[mavenbadge-svg]: https://badgen.net/maven/v/maven-central/com.yandex.scout/scout-core
[mavencentral]: https://search.maven.org/artifact/com.yandex.scout/scout-core

