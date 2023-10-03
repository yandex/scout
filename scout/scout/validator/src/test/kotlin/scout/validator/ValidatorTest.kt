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
package scout.validator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import scout.validator.model.ValidationResult
import scout.validator.tools.component.ComponentProducer

class ValidatorTest {

    @Test
    fun `Check that validator returns checker results`() {
        val result1 = ValidationResult.Passed("Some message about success")
        val result2 = ValidationResult.Failed("Some message about failure", emptyList())
        val checker1 = Checker { result1 }
        val checker2 = Checker { result2 }
        val validator = Validator(ComponentProducer.just(), listOf(checker1, checker2))
        assertThat(validator.validateWithResults()).isEqualTo(listOf(result1, result2))
    }

    @Test
    fun `Check that validator#validate fails if some checker failed`() {
        val result1 = ValidationResult.Passed("Some message about success")
        val result2 = ValidationResult.Failed("Some message about failure", emptyList())
        val checker1 = Checker { result1 }
        val checker2 = Checker { result2 }
        val validator = Validator(ComponentProducer.just(), listOf(checker1, checker2))
        assertThrows<ValidationException> { validator.validate() }
    }
}
