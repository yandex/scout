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
package scout.logging

abstract class Logger(private val level: Level = Level.INFO) {

    protected abstract fun log(level: Level, message: String)

    internal fun canLog(level: Level): Boolean {
        return level >= this.level
    }

    internal inline fun debug(message: () -> String) {
        if (canLog(Level.DEBUG)) {
            log(Level.DEBUG, message())
        }
    }

    internal inline fun info(message: () -> String) {
        if (canLog(Level.INFO)) {
            log(Level.INFO, message())
        }
    }

    internal inline fun warning(message: () -> String) {
        if (canLog(Level.WARNING)) {
            log(Level.WARNING, message())
        }
    }

    internal inline fun error(message: () -> String) {
        if (canLog(Level.ERROR)) {
            log(Level.ERROR, message())
        }
    }

    enum class Level {
        DEBUG, INFO, WARNING, ERROR, NONE,
    }
}
