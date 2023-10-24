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

import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ResultPageTest {

    @Test
    fun `should get page number from next URL`() {
        val nextPageNumber = ResultPage<Any>(
            count = 5,
            next = URI("https://test.local?page=1337"),
            results = listOf(),
        ).getNextPageNumber()

        assertThat(nextPageNumber).isEqualTo(1337)
    }

    @Test
    fun `should get fist page number from next URL`() {
        val nextPageNumber = ResultPage<Any>(
            count = 5,
            next = URI("https://test.local?page=1337&page=2337"),
            results = listOf(),
        ).getNextPageNumber()

        assertThat(nextPageNumber).isEqualTo(1337)
    }

    @Test
    fun `should get null from next URL for invalid page number`() {
        val nextPageNumber = ResultPage<Any>(
            count = 5,
            next = URI("https://test.local?page=xyz"),
            results = listOf(),
        ).getNextPageNumber()

        assertThat(nextPageNumber).isNull()
    }

    @Test
    fun `should get null from next URL for empty page number`() {
        val nextPageNumber = ResultPage<Any>(
            count = 5,
            next = URI("https://test.local?page="),
            results = listOf(),
        ).getNextPageNumber()

        assertThat(nextPageNumber).isNull()
    }

    @Test
    fun `should get null from next URL for missing page number`() {
        val nextPageNumber = ResultPage<Any>(
            count = 5,
            next = URI("https://test.local"),
            results = listOf(),
        ).getNextPageNumber()

        assertThat(nextPageNumber).isNull()
    }
}