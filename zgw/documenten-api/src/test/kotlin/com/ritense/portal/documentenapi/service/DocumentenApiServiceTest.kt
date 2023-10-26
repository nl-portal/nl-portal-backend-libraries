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
package com.ritense.portal.documentenapi.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.portal.documentenapi.client.DocumentApisConfig
import com.ritense.portal.documentenapi.client.DocumentenApiClient
import com.ritense.portal.documentenapi.domain.Document
import com.ritense.portal.documentenapi.domain.DocumentStatus
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@ExperimentalCoroutinesApi
@SpringBootTest
internal class DocumentenApiServiceTest(@Autowired private var documentApisConfig: DocumentApisConfig) {

    var documentenApiClient: DocumentenApiClient = mock()
    var documentenApiService: DocumentenApiService = DocumentenApiService(documentenApiClient, documentApisConfig)

    @Test
    fun `should find single document by UUID`() = runTest {
        val documentId = UUID.randomUUID()

        whenever(documentenApiClient.getDocument(documentId, "localhost")).thenReturn(
            getTestDocument(null),
        )

        val document = documentenApiService.getDocument(documentId, "localhost")

        verify(documentenApiClient, times(1)).getDocument(documentId, "localhost")

        assertDocumentReturned(document)
    }

    @Test
    fun `should find single document by URI String`() = runTest {
        val documentId = UUID.randomUUID()
        val documentURI = URI.create("https://example.org/documenten/api/v1/$documentId").toASCIIString()

        whenever(documentenApiClient.getDocument(documentId, "example")).thenReturn(
            getTestDocument(null),
        )

        val document = documentenApiService.getDocument(documentURI)

        verify(documentenApiClient, times(1)).getDocument(documentId, "example")

        assertDocumentReturned(document)
    }

    private fun getTestDocument(status: DocumentStatus?): Document {
        return Document(
            "http://example.com/enkelvoudiginformatieobjecten/0727b025-eaae-4587-a375-3fe671a19dd8",
            "identificatie",
            "2020-04-17",
            "titel",
            "text/plain",
            "bestandsnaam.txt",
            123,
            status,
        )
    }

    private fun assertDocumentReturned(document: Document) {
        assertEquals(
            "http://example.com/enkelvoudiginformatieobjecten/0727b025-eaae-4587-a375-3fe671a19dd8",
            document.url,
        )
        assertEquals("identificatie", document.identificatie)
        assertEquals("2020-04-17", document.creatiedatum)
        assertEquals("titel", document.titel)
        assertEquals("text/plain", document.formaat)
        assertEquals("bestandsnaam.txt", document.bestandsnaam)
        assertEquals(123, document.bestandsomvang)
    }
}