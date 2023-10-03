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
package scout.scope.access

import java.lang.IllegalArgumentException

internal class InterceptorRegistry {

    private var before: List<Interceptor.Before> = emptyList()

    private var beforeLazy: List<Interceptor.BeforeLazy> = emptyList()

    private var beforeProvider: List<Interceptor.BeforeProvider> = emptyList()

    private var after: List<Interceptor.After> = emptyList()

    private var afterLazy: List<Interceptor.AfterLazy> = emptyList()

    private var afterProvider: List<Interceptor.AfterProvider> = emptyList()

    fun register(interceptor: Interceptor) {
        var registered = false
        if (interceptor is Interceptor.Before) {
            before += interceptor
            registered = true
        }
        if (interceptor is Interceptor.BeforeLazy) {
            beforeLazy += interceptor
            registered = true
        }
        if (interceptor is Interceptor.BeforeProvider) {
            beforeProvider += interceptor
            registered = true
        }
        if (interceptor is Interceptor.After) {
            after += interceptor
            registered = true
        }
        if (interceptor is Interceptor.AfterLazy) {
            afterLazy += interceptor
            registered = true
        }
        if (interceptor is Interceptor.AfterProvider) {
            afterProvider += interceptor
            registered = true
        }
        if (!registered) {
            throw IllegalArgumentException(
                "Interceptor registration failed: $interceptor should implement " +
                        "one of ${Interceptor::class.simpleName} subtypes"
            )
        }
    }

    fun unregister(interceptor: Interceptor) {
        if (interceptor is Interceptor.Before) {
            before -= interceptor
        }
        if (interceptor is Interceptor.BeforeLazy) {
            beforeLazy -= interceptor
        }
        if (interceptor is Interceptor.BeforeProvider) {
            beforeProvider -= interceptor
        }
        if (interceptor is Interceptor.After) {
            after -= interceptor
        }
        if (interceptor is Interceptor.AfterLazy) {
            afterLazy -= interceptor
        }
        if (interceptor is Interceptor.AfterProvider) {
            afterProvider -= interceptor
        }
    }

    fun clear() {
        before = emptyList()
        beforeLazy = emptyList()
        beforeProvider = emptyList()
        after = emptyList()
        afterLazy = emptyList()
        afterProvider = emptyList()
    }

    fun isEmpty() = before.isEmpty() &&
            beforeLazy.isEmpty() &&
            beforeProvider.isEmpty() &&
            after.isEmpty() &&
            afterLazy.isEmpty() &&
            afterProvider.isEmpty()

    fun snapshotRegular() = Snapshot(before, after)

    fun snapshotLazy() = Snapshot(beforeLazy, afterLazy)

    fun snapshotProvider() = Snapshot(beforeProvider, afterProvider)

    data class Snapshot<B, A>(
        val before: List<B>,
        val after: List<A>
    )
}
