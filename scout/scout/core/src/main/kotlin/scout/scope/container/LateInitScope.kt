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
package scout.scope.container

import scout.Scope
import scout.Scout
import scout.scope
import scout.scope.builder.ScopeBuilder
import scout.scope.utils.ScopeUtils
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

/**
 * This class is useful for holding thread safe reference to lazily initialized scopes. Example:
 *
 *     val applicationScope = lateInitScope("appScope")
 *
 *     fun initializeDependencyGraph() {
 *         applicationScope.init {
 *             // ...
 *         }
 *     }
 *
 *     class StartScreen {
 *         val component = ScreenComponent(applicationScope.value)
 *         // ...
 *     }
 *
 * Also can be used like delegate for [Scope] property. Example:
 *
 *     val applicationScopeContainer = lateInitScope("appScope")
 *     val applicationScope: Scope by applicationScopeContainer
 *
 * If try to read value before initialization, [IllegalStateException] is thrown.
 * By default, this class allows initial value to be set only once. After that, if you try to set it again,
 * [IllegalStateException] is thrown. This can be disabled by passing `true` for [isOverwriteAllowed] parameter during
 * construction, or you can set it some time later. Disabling this check is mostly useful for tests.
 */
class LateInitScope(
    private val name: String,
    @Volatile var isOverwriteAllowed: Boolean = false
) {
    private val lock = Any()
    private var scope: Scope? = null

    val value: Scope
        get() {
            scope?.let { return it }
            return synchronized(lock) {
                checkNotNull(scope) {
                    "Late init ${ScopeUtils.formatIdentity(name)} was not initialized"
                }
            }
        }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Scope = value

    fun init(
        threadSafetyMode: ScopeBuilder.ThreadSafetyMode = Scout.ThreadSafety.scopeBuilderMode,
        configuration: ScopeBuilder.() -> Unit
    ) {
        synchronized(lock) {
            if (scope != null && !isOverwriteAllowed) {
                throw IllegalStateException("Late init ${ScopeUtils.formatIdentity(name)} is already initialized")
            }
            scope = scope(
                name = name,
                threadSafetyMode = threadSafetyMode,
                configuration = configuration
            )
        }
    }
}

/**
 * Create late init scope with synchronized access.
 * Scope should be initialized with [LateInitScope.init]
 * method call before first [LateInitScope.value] access.
 */
fun lateInitScope(
    name: String,
    isOverwriteAllowed: Boolean = false
) = LateInitScope(name, isOverwriteAllowed)
