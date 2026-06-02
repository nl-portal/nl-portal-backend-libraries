/*
 * Copyright 2015-2026 Ritense BV, the Netherlands.
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
package nl.nlportal.zgw.taak.web.rest

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.VirusScanResult
import nl.nlportal.documentenapi.domain.VirusScanStatus
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.documentenapi.service.VirusScanService
import nl.nlportal.zgw.taak.service.TaakService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import java.util.UUID

class TaakDocumentUploadResourceTest {
    private val taakService: TaakService = mock()
    private val documentenApiService: DocumentenApiService = mock()
    private val virusScanService: VirusScanService = mock()
    private val documentApisConfig: DocumentApisConfig =
        DocumentApisConfig().apply {
            properties.defaultDocumentApi = "openzaak"
        }
    private val authentication: CommonGroundAuthentication = mock()

    private lateinit var resource: TaakDocumentUploadResource

    @BeforeEach
    fun setUp() {
        resource = TaakDocumentUploadResource(taakService, documentenApiService, virusScanService, documentApisConfig)
    }

    @Test
    fun `throws 401 when user does not own taak`() {
        val taakId = UUID.randomUUID()
        val file = filePart("hello.pdf", "hello".toByteArray())
        runBlocking {
            whenever(taakService.getTaakByIdV2(taakId, authentication))
                .thenThrow(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied to this taak"))
        }

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                runBlocking { resource.uploadDocument(taakId, file, null, authentication) }
            }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun `sanitizes filename and passes buffered bytes to uploadDocument`() =
        runTest {
            val taakId = UUID.randomUUID()
            val payload = "PDFCONTENT".toByteArray()
            val file = filePart("../../etc/passwd", payload)
            whenever(virusScanService.scan(any<ByteArray>()))
                .thenReturn(VirusScanResult(VirusScanStatus.OK, emptyMap()))
            whenever(documentenApiService.uploadDocument(any(), any(), any(), anyOrNull()))
                .thenReturn(stubDocument())

            val response = resource.uploadDocument(taakId, file, null, authentication)

            assertEquals(HttpStatus.OK, response.statusCode)
            val filenameCaptor = argumentCaptor<String>()
            val bytesCaptor = argumentCaptor<ByteArray>()
            verify(documentenApiService).uploadDocument(
                bytesCaptor.capture(),
                filenameCaptor.capture(),
                any(),
                anyOrNull(),
            )
            assertFalse(filenameCaptor.firstValue.contains('/'))
            assertFalse(filenameCaptor.firstValue.contains('\\'))
            assertEquals(payload.toList(), bytesCaptor.firstValue.toList())
        }

    @Test
    fun `returns 400 when virus found`() =
        runTest {
            val taakId = UUID.randomUUID()
            val file = filePart("doc.pdf", "EICAR".toByteArray())
            whenever(virusScanService.scan(any<ByteArray>()))
                .thenReturn(VirusScanResult(VirusScanStatus.VIRUS_FOUND, mapOf("file" to setOf("EICAR-TEST"))))

            val response = resource.uploadDocument(taakId, file, null, authentication)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        }

    @Test
    fun `scan and uploadDocument receive same buffered ByteArray`() =
        runTest {
            val taakId = UUID.randomUUID()
            val payload = "SAME".toByteArray()
            val file = filePart("doc.pdf", payload)
            whenever(virusScanService.scan(any<ByteArray>()))
                .thenReturn(VirusScanResult(VirusScanStatus.OK, emptyMap()))
            whenever(documentenApiService.uploadDocument(any(), any(), any(), anyOrNull()))
                .thenReturn(stubDocument())

            resource.uploadDocument(taakId, file, null, authentication)

            val scanCaptor = argumentCaptor<ByteArray>()
            val uploadCaptor = argumentCaptor<ByteArray>()
            verify(virusScanService).scan(scanCaptor.capture())
            verify(documentenApiService).uploadDocument(
                uploadCaptor.capture(),
                any(),
                any(),
                anyOrNull(),
            )
            assertSame(scanCaptor.firstValue, uploadCaptor.firstValue)
        }

    private fun filePart(
        name: String,
        content: ByteArray,
    ): FilePart {
        val factory = DefaultDataBufferFactory()
        val buffer: DataBuffer = factory.wrap(content)
        val part: FilePart = mock()
        whenever(part.filename()).thenReturn(name)
        whenever(part.content()).thenReturn(Flux.just(buffer))
        return part
    }

    private fun stubDocument() =
        Document(
            url = "http://example.com/enkelvoudiginformatieobjecten/${UUID.randomUUID()}",
            identificatie = "id",
            creatiedatum = "2024-01-01",
            titel = "t",
            formaat = "application/pdf",
            bestandsnaam = "doc.pdf",
            bestandsomvang = 1,
            status = null,
            vertrouwelijkheidaanduiding = null,
        )
}
