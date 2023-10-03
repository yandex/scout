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

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import scout.utils.ThreadConfinementCheck
import scout.utils.runBlockingInDifferentThread
import java.util.concurrent.ExecutionException

class ThreadConfinementCheckTest {

    private val checkThread = ThreadConfinementCheck()

    @Test
    fun `Throws exception when being invoked from multiple threads`() {
        assertThatExceptionOfType(ExecutionException::class.java)
            .isThrownBy {
                checkThread()

                runBlockingInDifferentThread {
                    checkThread()
                }
            }
            .withCauseInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Do not throw exception when being invoked from same thread`() {
        checkThread()
        checkThread()
    }
}
