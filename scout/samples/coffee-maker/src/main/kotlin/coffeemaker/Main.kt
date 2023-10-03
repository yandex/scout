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
package coffeemaker

import coffeemaker.entity.CoffeeMaker
import coffeemaker.entity.ElectricHeater
import coffeemaker.entity.Heater
import coffeemaker.entity.Pump
import coffeemaker.entity.Thermosiphon
import scout.Component
import scout.scope

private val appScope = scope("app-scope") {
    singleton<CoffeeMaker> { CoffeeMaker(pump = get(), heater = get()) }
    singleton<Pump> { Thermosiphon(heater = get()) }
    singleton<Heater> { ElectricHeater() }
}

private object AppComponent : Component(appScope) {
    fun coffeeMaker(): CoffeeMaker = get()
}

fun main() = AppComponent
    .coffeeMaker()
    .brew()
