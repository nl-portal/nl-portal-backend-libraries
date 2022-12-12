/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.zaak.web.rest

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.portal.zaak.client.OpenZaakClient
import com.ritense.portal.zaak.domain.documenten.Document
import com.ritense.portal.zaak.service.ZaakService
import com.ritense.portal.zaak.web.rest.impl.DocumentContentResource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

class DocumentContentResourceTest {

    val openZaakClient = mock<OpenZaakClient>()
    val zaakService = mock<ZaakService>()
    val downloadResource = DocumentContentResource(openZaakClient, zaakService)
    val document = mock<Document>()

    @Test
    fun `should fill http servlet response`() = runTest {
        val uuid = UUID.randomUUID()
        val testString = "This is a test string for the DataBuffer, it should end up in the result"
        val fluxDataBuffer = getFluxDataBufferFromString(testString)

        doReturn(fluxDataBuffer).`when`(openZaakClient).getDocumentContentStream(uuid)
        whenever(zaakService.getDocument(uuid)).thenReturn(document)
        whenever(document.bestandsnaam).thenReturn("bestandsnaam.png")

        val result = downloadResource.downloadStreaming(uuid)

        val bodyByteArray = result.body.let { it?.blockLast()?.asByteBuffer()?.array() }
        assertNotNull(bodyByteArray)
        val resultString = String(bodyByteArray!!, StandardCharsets.UTF_8)

        verify(openZaakClient).getDocumentContentStream(uuid)
        assertEquals(testString, resultString)
    }

    fun getFluxDataBufferFromString(value: String): Flux<DataBuffer> {
        val dataBuffer = DefaultDataBufferFactory()
            .wrap(
                ByteBuffer
                    .wrap(value.toByteArray(Charset.defaultCharset()))
            )

        return Flux.just(dataBuffer) as Flux<DataBuffer>
    }
}