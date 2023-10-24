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
package com.ritense.portal.documentenapi.graphql

import com.nhaarman.mockitokotlin2.mock
import com.ritense.portal.documentenapi.service.DocumentenApiService
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
internal class DocumentContentQueryTest {

    var documentenApiService: DocumentenApiService = mock()
    var documentContentQuery = DocumentContentQuery(documentenApiService)

    @Test
    fun getDocumentContent() = runTest {
        val documentId = UUID.randomUUID()
        documentContentQuery.getDocumentContent("openzaak", documentId)
        verify(documentenApiService).getDocumentContent(documentId, "openzaak")
    }
}