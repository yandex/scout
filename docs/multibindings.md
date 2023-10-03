# Multibindings

Scout allows you to bind several objects into a collection even when the objects are bound in 
different scopes using multibindings. Scout assembles collection so that application code can 
inject it without depending directly on the individual bindings.

## List multibindings

In order to contribute elements to a list, add `element` factories to scope:
```kotlin
val rhymeScope = scope("rhyme-scope") {
    element<String> { "foo" }
    element<String> { "bar" }
    element<String> { "baz" }
}
```

Now you can collect elements into list from this scope:
```kotlin
val rhymeScope = scope("rhyme-scope") {
    ...
    factory<CountingRhyme> {
        CountingRhyme(rhymes = collect()) // 'rhymes' property has List<String> type
    }
}
```

You also can collect list of elements in component:
```kotlin
class RhymesComponent : Component(appScope) {
    fun rhymes() = collect<String>()
}
```

## Map multibindings

Scout lets you use multibindings to contribute entries to a map. To achieve it, add `mapping` 
factories to scope:
```kotlin
val helloScope = scope("hello-scope") {
    mapping<String, String> { "en" to "Hello" }
    mapping<String, String> { "fr" to "Bonjour" }
    mapping<String, String> { "it" to "Ciao" }
}
```

Now you can collect mappings into map from this scope:
```kotlin
val helloScope = scope("hello-scope") {
    ...
    factory<Translations> {
        Translations(langs = associate()) // 'lang' property has Map<String, String> type
    }
}
```

You also can collect mappings in component:
```kotlin
class HelloComponent : Component(helloScope) {
    fun translations() = associate<String, String>()
}
```

