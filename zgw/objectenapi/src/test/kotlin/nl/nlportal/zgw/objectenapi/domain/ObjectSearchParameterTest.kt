/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.nlportal.zgw.objectenapi.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ObjectSearchParameterTest {
    @Test
    fun `should create query parameter for multiple search parameter objects`() {
        val queryParameter = ObjectSearchParameter.toQueryParameter(
            listOf(
                ObjectSearchParameter("bsn", Comparator.EQUAL_TO, "123456789"),
                ObjectSearchParameter("bsn", Comparator.EQUAL_TO, "987654321")
            )
        )

        assertThat(queryParameter).isEqualTo("bsn__exact__123456789,bsn__exact__987654321")
    }

    @Test
    fun `should create empty query parameter for empty list`() {
        val queryParameter = ObjectSearchParameter.toQueryParameter(
            listOf()
        )

        assertThat(queryParameter).isEqualTo("")
    }
}