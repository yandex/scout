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
package scout.validator.tools.component

import scout.Component
import scout.validator.tools.instance.InstanceProducer

/**
 * This interface defines producer of [Component] sequence.
 */
fun interface ComponentProducer {

    /**
     * Produce [Component] iterable.
     */
    fun produce(): Iterable<Component>

    companion object {

        /**
         * Create provider with passed [Component] instances.
         */
        fun just(vararg items: Component): ComponentProducer {
            val memoized = items.toList()
            return ComponentProducer { memoized }
        }

        /**
         * Create provider with passed [Component] instances.
         */
        fun just(items: List<Component>): ComponentProducer {
            val unmodifiable = items.toList()
            return ComponentProducer { unmodifiable }
        }

        /**
         * Create provider with instances created by [types] using [producer].
         */
        fun from(
            types: List<Class<out Component>>,
            producer: InstanceProducer<Component>
        ): ComponentProducer {
            val components = types.map { type -> producer.produce(type) }
            return ComponentProducer { components }
        }
    }
}
