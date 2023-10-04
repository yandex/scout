# Quick Start
This tutorial lets you write a Kotlin application and use Scout dependency injection to retrieve your dependencies. You will learn about scopes, components and factories, will get to know the basic library syntax.

## Setup
First, add the Scout dependency to your gradle script:
```groovy
dependencies {
   implementation "com.yandex.scout:scout-core:$scout_version"
}
```

## Application Overview
The idea of application is to find books available for downloading using several open APIs, and display it.

### Models
```kotlin
data class Book(
    val title: String,
    val authors: List<String>,
    val downloadLink: String
)
```

### Repository (interface and implementation)
```kotlin
interface BookSearchRepository {
    fun findBooks(title: String): List<Book>
}

class BookSearchRepositoryImpl(
    private val bookStoreApis: List<BookStoreApi>
) : BookSearchRepository {

    override fun findBooks(title: String): List<Book> {
        return bookStoreApis.map { api -> api.findBooks(title) }
            .flatten()
            .filter { book -> book.downloadLink.isNotEmpty() }
            .distinctBy { book -> book.downloadLink }
    }
}
```

### Store APIs (interface and implementations)
```kotlin
interface BookStoreApi {
    fun findBooks(title: String): List<Book>
}

class FirstBookStoreApi(private val httpClient: HttpClient) : BookStoreApi { ... }
class SecondBookStoreApi(private val httpClient: HttpClient) : BookStoreApi { ... }
class ThirdBookStoreApi(private val httpClient: HttpClient) : BookStoreApi { ... }
```

### Formatter
```kotlin
class BookFormatter {

    fun format(book: Book) = buildString {
        appendLine(book.title)
        appendLine(book.authors.joinToString())
        appendLine("Download: ${book.downloadLink}")
    }
}
```

## Let's build dependency graph

Scope represents dependency graph, so we should create at least one.
```kotlin
val appScope = scope("app-scope") { // declare application dependency graph
    
}
```

Scope should contain factory for `BookSearchRepository`.
```kotlin
val appScope = scope("app-scope") {
   factory<BookSearchRepository> { // register BookSearchRepository factory
        BookSearchRepositoryImpl(
            bookStoreApis = collect() // collect all registered APIs
        )
    }
}
```

Repository factory attempts to collect API implementations in scope, so we need to register some `BookStoreApi` implementations.
```kotlin
val appScope = scope("app-scope") {
    ...
    element<BookStoreApi> { // Register element for list of APIs
        FirstBookStoreApi(httpClient = get())
    }
    element<BookStoreApi> { // Register element for list of APIs
        SecondBookStoreApi(httpClient = get())
    }
    element<BookStoreApi> { // Register element for list of APIs
        ThirdBookStoreApi(httpClient = get())
    }
    singleton<HttpClient> { // Register HttpClient for APIs
        HttpClient()
    }
}
```

Now we are ready to define our component. There should be two public dependencies: `BookSearchRepository` and `BookFormatter`.
```kotlin
object AppComponent : Component(appScope) {
    fun searchRepository(): BookSearchRepository = get()
    fun bookFormatter(): BookFormatter = get()
}
```

## It's time to launch
```kotlin
class BookSearchApplication {
    private val bookSearchRepository by lazy { AppComponent.searchRepository() }
    private val bookFormatter by lazy { AppComponent.bookFormatter() }

    fun search(title: String) {
        val books = bookSearchRepository.findBooks(title)
        for (book in books) {
            println(bookFormatter.format(book))
        }
    }
}

fun main() = BookSearchApplication()
    .findBooks("Catcher in the rye")
```
When we will press the `▶` button, we will face a problem. Programm finished with error, because we forgot to register factory for `BookFormatter`.
```
Exception in thread "main" scout.exception.MissingObjectFactoryException: Missing factory for Object(type=BookFormatter)
Tree of scopes:
   ⌞ Scope(name="app-scope") (object factories: 2, collection factories: 1, association factories: 0, allowed object overrides: 0)
	at scout.Scope.getObject$core(Scope.kt:159)
	at scout.scope.access.Accessor.get(Accessor.kt:79)
   ...

Process finished with exit code 1
```

## I want to protect myself
To test our dependency graph we need to add validator module:
```groovy
dependencies {
   testImplementation "com.yandex.scout:scout-validator:$scout_version"
}
```
Test will look like:
```kotlin
class DependencyGraphTest {
    @Test
    fun `Test dependency graph`() {
        Validator.configure()
            .withConsistencyCheck()
            .validate(ComponentProducer.just(AppComponent))
    }
}
```
Now we can make sure that the error in the dependency graph is found by the test:
```
Validate 1 components using ConsistencyChecker
 ✘ Consistency check failed with 1 errors

Exception in thread "main" scout.validator.ValidationException: 

[Consistency check failed with 1 errors]

  ✘ [An error occurred while calling of method AppComponent.bookFormatter(AppComponent.kt:0)] (1 errors)
	scout.exception.MissingObjectFactoryException: Missing factory for Object(type=BookFormatter)
	Tree of scopes:
	   ⌞ Scope(name="app-scope") (object factories: 2, collection factories: 1, association factories: 0, allowed object overrides: 0)
		at scout.Scope.getObject$core(Scope.kt:159)
		at scout.scope.access.Accessor.get(Accessor.kt:79)

Process finished with exit code 1
```
Let's register factory for `BookFormater` to fix the program.
```kotlin
reusable<BookFormatter> { // register reusable instance of BookFormatter type
    BookFormatter()
}
```
Once we fixed the problem, the test started to pass.
```
Validate 1 components using ConsistencyChecker
 ✔ Consistency check passed without errors

Process finished with exit code 0
```

## Give me some beauty
The last thing left to do is to decompose the scope declaration so that in the future the scope configuration does not turn into one huge method. Factory registrations can be separated into extension methods on type `Registry`.
```kotlin
val appScope = scope("app-scope") {
    useNetworkBeans()
    useStoreApiBeans()
    useRepositoryBeans()
    usePresentationBeans()
}

fun Registry.useRepositoryBeans() {
    factory<BookSearchRepository> {
        BookSearchRepositoryImpl(
            bookStoreApis = collect()
        )
    }
}

fun Registry.useStoreApiBeans() {
    element<BookStoreApi> {
        FirstBookStoreApi(httpClient = get())
    }
    element<BookStoreApi> {
        SecondBookStoreApi(httpClient = get())
    }
    element<BookStoreApi> {
        ThirdBookStoreApi(httpClient = get())
    }
}

fun Registy.useNetworkBeans() {
    singleton<HttpClient> {
        HttpClient()
    }
}

fun Registry.usePresentationBeans() {
    reusable<BookFormatter> {
        BookFormatter()
    }
}
```
