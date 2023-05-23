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
package com.ritense.portal.catalogiapi.client

import com.ritense.portal.core.util.Mapper
import com.ritense.portal.documentenapi.domain.Document
import com.ritense.portal.documentenapi.domain.PostEnkelvoudiginformatieobjectRequest
import com.ritense.portal.idtokenauthentication.service.IdTokenGenerator
import io.netty.handler.logging.LogLevel
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.Base64
import java.util.UUID
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class DocumentenApiClient(
    private val documentenApiConfig: DocumentenApiConfig,
    private val idTokenGenerator: IdTokenGenerator
) {
    suspend fun getDocument(id: UUID): Document {
        return webClient()
            .get()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten/$id")
            .retrieve()
            .awaitBody()
    }

    suspend fun getDocumentContent(id: UUID): ByteArray {
        return webClient()
            .get()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten/$id/download")
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .awaitBody()
    }

    fun getDocumentContentStream(id: UUID): Flux<DataBuffer> {
        return webClient()
            .get()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten/$id/download")
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlux(DataBuffer::class.java)
    }

    suspend fun postDocument(
        request: PostEnkelvoudiginformatieobjectRequest,
        documentContent: Flux<DataBuffer>
    ): Document {
        request.inhoud = UUID.randomUUID().toString()
        val (requestPrefix, requestPostfix) = Mapper.get().writeValueAsString(request).split(request.inhoud!!)

        val file = withContext(Dispatchers.IO) {
            Files.createTempFile("tempDocumentUploadRequest", ".json")
        }

        file.writeText(requestPrefix)
        Base64.getEncoder().wrap(file.outputStream(StandardOpenOption.APPEND)).use { base64Output ->
            documentContent
                .map { dataPart -> base64Output.write(dataPart.asInputStream().readBytes()) }
                .blockLast(Duration.ofMinutes(5))
        }
        file.writeText(requestPostfix, Charsets.UTF_8, StandardOpenOption.APPEND)

        val response = webClient()
            .post()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromResource(FileSystemResource(file)))
            .retrieve()
            .awaitBody<Document>()

        file.deleteIfExists()
        return response
    }

    private fun webClient(): WebClient {
        val token = idTokenGenerator.generateToken(
            documentenApiConfig.secret,
            documentenApiConfig.clientId
        )

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL
                    )
                )
            )
            .baseUrl(documentenApiConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}