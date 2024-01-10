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
package nl.nlportal.documentenapi.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.net.URI

internal class ResultPageTest {
    @Test
    fun `getNextPageNumber returns page number from querystring param in next url`() {
        val page = ResultPage(3, URI("http://exmaple.com/some-path?page=2"), null, listOf("test 1", "test 2"))

        assertEquals(2, page.getNextPageNumber())
    }

    @Test
    fun `getNextPageNumber returns null when next is null`() {
        val page = ResultPage(2, null, null, listOf("test 1", "test 2"))

        assertNull(page.getNextPageNumber())
    }
}