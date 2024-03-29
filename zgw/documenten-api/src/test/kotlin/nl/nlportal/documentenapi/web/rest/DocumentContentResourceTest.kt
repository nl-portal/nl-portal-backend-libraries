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
package nl.nlportal.documentenapi.web.rest

import nl.nlportal.documentenapi.client.DocumentenApiClient
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.documentenapi.service.VirusScanService
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux

@ExperimentalCoroutinesApi
class DocumentContentResourceTest {
    private val documentenApiClient: DocumentenApiClient = mock()
    private val documentenApiService: DocumentenApiService = mock()
    private val virusScanService: VirusScanService = mock()
    private val documentApisConfig: DocumentApisConfig = mock()
    private val downloadResource = DocumentContentResource(documentenApiClient, documentenApiService, virusScanService, documentApisConfig)
    val document: Document = mock()

    @Test
    fun `should fill http servlet response`() =
        runTest {
            val uuid = UUID.randomUUID()
            val testString = "This is a test string for the DataBuffer, it should end up in the result"
            val fluxDataBuffer = getFluxDataBufferFromString(testString)

            doReturn(fluxDataBuffer).`when`(documentenApiClient).getDocumentContentStream(uuid, "localhost")
            whenever(documentenApiService.getDocument(uuid, "localhost")).thenReturn(document)
            whenever(document.bestandsnaam).thenReturn("bestandsnaam.png")

            val result = downloadResource.downloadStreaming(uuid, "localhost")

            val bodyByteArray = result.body.let { it?.blockLast()?.asByteBuffer()?.array() }
            assertNotNull(bodyByteArray)
            val resultString = String(bodyByteArray!!, StandardCharsets.UTF_8)

            verify(documentenApiClient).getDocumentContentStream(uuid, "localhost")
            assertEquals(testString, resultString)
        }

    fun getFluxDataBufferFromString(value: String): Flux<DataBuffer> {
        val dataBuffer =
            DefaultDataBufferFactory()
                .wrap(
                    ByteBuffer
                        .wrap(value.toByteArray(Charset.defaultCharset())),
                )

        return Flux.just(dataBuffer) as Flux<DataBuffer>
    }
}