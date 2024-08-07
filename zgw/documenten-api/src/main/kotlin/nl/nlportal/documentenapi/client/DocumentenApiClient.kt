/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
package nl.nlportal.documentenapi.client

import io.netty.handler.logging.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.PostEnkelvoudiginformatieobjectRequest
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlow
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.util.Base64
import java.util.UUID

class DocumentenApiClient(
    private val documentenApiConfigs: DocumentApisConfig,
    private val idTokenGenerator: IdTokenGenerator,
    private val clientSslContextResolver: ClientSslContextResolver? = null,
) {
    suspend fun getDocument(
        id: UUID,
        documentApi: String,
    ): Document {
        val document: Document =
            try {
                webClient(documentApi)
                    .get()
                    .uri("/enkelvoudiginformatieobjecten/$id")
                    .retrieve()
                    .awaitBody()
            } catch (e: WebClientResponseException) {
                logger.error("Could not retrieve document: ${e.responseBodyAsString}", e)
                throw RuntimeException("Could not retrieve document: ${e.responseBodyAsString}", e)
            }
        document.documentapi = documentApi
        return document
    }

    suspend fun getDocumentContent(
        id: UUID,
        documentApi: String,
    ): ByteArray {
        return webClient(documentApi)
            .get()
            .uri("/enkelvoudiginformatieobjecten/$id/download")
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .awaitBody()
    }

    fun getDocumentContentStream(
        id: UUID,
        documentApi: String,
    ): Flow<DataBuffer> {
        return webClient(documentApi)
            .get()
            .uri("/enkelvoudiginformatieobjecten/$id/download")
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlow()
    }

    suspend fun postDocument(
        request: PostEnkelvoudiginformatieobjectRequest,
        documentContent: Flux<DataBuffer>,
        documentApi: String,
    ): Document {
        request.inhoud = UUID.randomUUID().toString()

        val documentContentStream = DataBufferUtils.join(documentContent).awaitSingleOrNull()?.asInputStream()

        request.inhoud =
            withContext(Dispatchers.IO) {
                Base64.getEncoder()
                    .encodeToString(
                        documentContentStream?.readAllBytes(),
                    )
            }

        val response =
            webClient(documentApi)
                .post()
                .uri("/enkelvoudiginformatieobjecten")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(
                    BodyInserters
                        .fromValue(request),
                )
                .retrieve()
                .awaitBody<Document>()

        return response
    }

    private fun webClient(documentApi: String): WebClient {
        val documentenApiConfig = documentenApiConfigs.getConfig(documentApi)

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL,
                    ).let { client ->
                        var result = client
                        if (clientSslContextResolver != null) {
                            documentenApiConfig.ssl?.let {
                                val sslContext =
                                    clientSslContextResolver.resolve(
                                        it.key,
                                        it.trustedCertificate,
                                    )

                                result = client.secure { builder -> builder.sslContext(sslContext) }

                                logger.debug { "Client SSL context was set: private key=${it.key != null}, trusted certificate=${it.trustedCertificate != null}." }
                            }
                        }
                        result
                    },
                ),
            )
            .baseUrl(documentenApiConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .apply {
                if (!documentenApiConfig.secret.isNullOrBlank() && !documentenApiConfig.clientId.isNullOrBlank()) {
                    // only add an authorization header if there is a secret and clientId
                    val token =
                        idTokenGenerator.generateToken(documentenApiConfig.secret!!, documentenApiConfig.clientId!!)
                    it.defaultHeader("Authorization", "Bearer $token")
                }
            }
            .build()
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}