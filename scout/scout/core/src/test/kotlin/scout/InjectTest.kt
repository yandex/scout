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

class InjectTest {

    @Test
    fun `Inject provides instance from component scope`() {
        val scope = scope("test-scope") {
            factory<String> { "Hello, world!" }
        }

        class MyComponent : Component(scope) {

            fun string(): String = get()
        }

        val myComponent = MyComponent()

        class UsageCase {
            val myString by inject(myComponent::string)
            val myStringLazy by injectLazy(myComponent::string)
            val myStringProvider by injectProvider(myComponent::string)
        }

        val usageCase = UsageCase()
        val myString = usageCase.myString
        val myStringLazy = usageCase.myStringLazy
        val myStringProvider = usageCase.myStringProvider

        assertThat(myString).isEqualTo("Hello, world!")
        assertThat(myStringLazy.value).isEqualTo("Hello, world!")
        assertThat(myStringProvider.get()).isEqualTo("Hello, world!")
    }

    @Test
    fun `Inject provides instance from injector scope`() {
        val scope = scope("test-scope") {
            factory<String> { "Hello, world!" }
        }

        class MyInjector : Injector<String>(scope) {
            override fun create(): String = get()
        }

        class UsageCase {
            val myString by inject(MyInjector())
            val myStringLazy by injectLazy(MyInjector())
            val myStringProvider by injectProvider(MyInjector())
        }

        val usageCase = UsageCase()
        val myString = usageCase.myString
        val myStringLazy = usageCase.myStringLazy
        val myStringProvider = usageCase.myStringProvider

        assertThat(myString).isEqualTo("Hello, world!")
        assertThat(myStringLazy.value).isEqualTo("Hello, world!")
        assertThat(myStringProvider.get()).isEqualTo("Hello, world!")
    }
}
