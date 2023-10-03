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

import kotlin.reflect.KProperty

typealias InjectInitializer<T> = () -> T

/**
 * Inject instance of type T using specified [InjectInitializer].
 */
fun <T> inject(initializer: InjectInitializer<T>): Inject<T> {
    return UnsafeInjectImpl(
        initializer = initializer
    )
}

/**
 * Inject lazy instance of type T using specified [InjectInitializer].
 */
fun <T> injectLazy(initializer: InjectInitializer<T>): Inject<Lazy<T>> {
    return UnsafeInjectImpl(
        initializer = { lazy(initializer) }
    )
}

/**
 * Inject provider of instance of type T using specified [InjectInitializer].
 */
fun <T> injectProvider(initializer: InjectInitializer<T>): Inject<Provider<T>> {
    return UnsafeInjectImpl(
        initializer = { Provider(initializer) }
    )
}

/**
 * Inject synchronously instance of type T using specified [InjectInitializer].
 */
fun <T> synchronizedInject(initializer: InjectInitializer<T>): Inject<T> {
    return SynchronizedInjectImpl(
        initializer = initializer
    )
}

/**
 * Inject synchronously lazy instance of type T using specified [InjectInitializer].
 */
fun <T> synchronizedInjectLazy(initializer: InjectInitializer<T>): Inject<Lazy<T>> {
    return SynchronizedInjectImpl(
        initializer = { lazy(initializer) }
    )
}

/**
 * Inject synchronously provider of instance of type T using specified [InjectInitializer].
 */
fun <T> synchronizedInjectProvider(initializer: InjectInitializer<T>): Inject<Provider<T>> {
    return SynchronizedInjectImpl(
        initializer = { Provider(initializer) }
    )
}

/**
 * Allows to inject values into properties.
 * ```
 * private val myVar by inject(MyInjector())
 * ```
 */
interface Inject<T> {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T

    fun isInitialized(): Boolean
}

private class SynchronizedInjectImpl<T>(initializer: InjectInitializer<T>, lock: Any? = null) : Inject<T> {
    @Volatile
    private var _value: Any? = NotInitializedValue
    private var initializer: InjectInitializer<T>? = initializer
    private val lock = lock ?: this

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val current = _value
        if (current !== NotInitializedValue) {
            @Suppress("UNCHECKED_CAST")
            return current as T
        }

        return synchronized(lock) {
            val current2 = _value
            if (current2 !== NotInitializedValue) {
                @Suppress("UNCHECKED_CAST") (current2 as T)
            } else {
                val newValue = requireNotNull(initializer)()
                _value = newValue
                initializer = null
                newValue
            }
        }
    }

    override fun isInitialized(): Boolean = _value !== NotInitializedValue

    override fun toString(): String {
        return if (isInitialized()) _value.toString() else INJECT_VALUE_NOT_INITIALIZED_YET
    }
}

private class UnsafeInjectImpl<T>(initializer: InjectInitializer<T>) : Inject<T> {
    private var initializer: InjectInitializer<T>? = initializer
    private var _value: Any? = NotInitializedValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (_value === NotInitializedValue) {
            _value = requireNotNull(initializer)()
            initializer = null
        }
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    override fun isInitialized(): Boolean = _value !== NotInitializedValue

    override fun toString(): String {
        return if (isInitialized()) _value.toString() else INJECT_VALUE_NOT_INITIALIZED_YET
    }
}

private object NotInitializedValue

private const val INJECT_VALUE_NOT_INITIALIZED_YET = "Inject value not initialized yet."
