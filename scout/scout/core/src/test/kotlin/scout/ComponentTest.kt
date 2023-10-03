/*
 * Copyright 2023 Yandex LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scout

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import scout.exception.MissingObjectFactoryException

class ComponentTest {

    @Test
    fun `Check method 'get' returns definition from scope`() {
        val component = GreetingComponent()
        val text = component.text()
        val greeting = component.greeting()
        assertThat(text).isEqualTo(GREETING_TEXT)
        assertThat(greeting).isEqualTo(Greeting(GREETING_TEXT))
    }

    @Test
    fun `Check method 'assisted' returns definition from assistance lambda and allows to use accessor`() {
        val prefix = "Let's say: "
        val component = GreetingComponent()
        val greeting = component.greetingWithPrefix(prefix)
        assertThat(greeting).isEqualTo(Greeting(prefix + GREETING_TEXT))
    }

    @Test
    fun `Check method 'get' throws error if definition not found`() {
        val component = GreetingComponent()
        assertThrows<MissingObjectFactoryException> {
            component.illegalAutoNumber()
        }
    }

    @Test
    fun `Check method 'assisted' throws error if definition not found`() {
        val component = GreetingComponent()
        assertThrows<MissingObjectFactoryException> {
            component.illegalAssistedNumber()
        }
    }

    @Test
    fun `Check method 'collection' returns defined collection`() {
        val component = FeaturesComponent()
        val features = component.features()
        assertThat(features).containsExactly("foo", "bar", "baz")
    }

    @Test
    fun `Check method 'collection' returns defined collection lazy`() {
        val component = FeaturesComponent()
        val features = component.featuresLazy().value
        assertThat(features).containsExactly("foo", "bar", "baz")
    }

    @Test
    fun `Check method 'collection' returns defined collection provider`() {
        val component = FeaturesComponent()
        val features = component.featuresProvider().get()
        assertThat(features).containsExactly("foo", "bar", "baz")
    }
}

private const val GREETING_TEXT = "Hello, world!"

private data class Greeting(val text: String)

private val greetingScope = scope("greeting-scope") {
    factory<String> { GREETING_TEXT }
    factory<Greeting> { Greeting(text = get()) }
}

private class GreetingComponent : Component(greetingScope) {

    fun text() = get<String>()

    fun greeting() = get<Greeting>()

    fun greetingWithPrefix(prefix: String): Greeting {
        val text = get<String>()
        return Greeting(prefix + text)
    }

    fun illegalAutoNumber() = get<Int>()

    fun illegalAssistedNumber() = get<Int>()
}

private val featureScope = scope("features-scope") {
    element<String> { "foo" }
    element<String> { "bar" }
    element<String> { "baz" }
}

private class FeaturesComponent : Component(featureScope) {

    fun features() = collect<String>()
    fun featuresLazy() = collectLazy<String>()
    fun featuresProvider() = collectProvider<String>()
    fun illegalCollection() = collect<Int>()
}
