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
package nl.nlportal.verificatie.client

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.handler.logging.LogLevel
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.core.util.Mapper
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import nl.nlportal.verificatie.autoconfigure.VerificatieModuleConfiguration.VerificatieConfigurationProperties
import nl.nlportal.verificatie.client.domain.VerificatieCreateApiRequest
import nl.nlportal.verificatie.client.domain.VerificatieCreateApiResponse
import nl.nlportal.verificatie.client.domain.VerificatieVerifyApiRequest
import nl.nlportal.verificatie.client.domain.VerificatieVerifyApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.server.ResponseStatusException
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class VerificatieClient(
    private val verificatieConfigurationProperties: VerificatieConfigurationProperties,
    private val clientSslContextResolver: ClientSslContextResolver? = null,
) {
    val webClient: WebClient

    init {
        this.webClient =
            WebClient
                .builder()
                .clone()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient
                            .create()
                            .wiretap(
                                "reactor.netty.http.client.HttpClient",
                                LogLevel.TRACE,
                                AdvancedByteBufFormat.TEXTUAL,
                            ).let { client ->
                                var result = client
                                if (clientSslContextResolver != null) {
                                    verificatieConfigurationProperties.ssl?.let {
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
                ).apply {
                    val token = getToken()
                    if (token != null) {
                        it.defaultHeader("Authorization", "Bearer $token")
                    }
                }.exchangeStrategies(
                    ExchangeStrategies
                        .builder()
                        .codecs { configurer ->
                            with(configurer.defaultCodecs()) {
                                maxInMemorySize(16 * 1024 * 1024)
                                jackson2JsonEncoder(
                                    Jackson2JsonEncoder(Mapper.get()),
                                )
                                jackson2JsonDecoder(
                                    Jackson2JsonDecoder(Mapper.get()),
                                )
                            }
                        }.build(),
                ).baseUrl(verificatieConfigurationProperties.url.toString())
                .build()
    }

    suspend fun create(
        request: VerificatieCreateApiRequest,
    ): VerificatieCreateApiResponse =
        webClient
            .post()
            .uri("/api/verification-requests")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue(request),
            ).retrieve()
            .handleStatus()
            .awaitBody<VerificatieCreateApiResponse>()

    suspend fun verify(
        request: VerificatieVerifyApiRequest,
    ): VerificatieVerifyApiResponse =
        webClient
            .post()
            .uri("/api/verification-requests/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue(request),
            ).retrieve()
            .handleStatus()
            .awaitBody<VerificatieVerifyApiResponse>()

    private fun getToken(): String? {
        if (verificatieConfigurationProperties.clientId != null && verificatieConfigurationProperties.secret != null) {
            return IdTokenGenerator().generateToken(
                verificatieConfigurationProperties.secret!!,
                verificatieConfigurationProperties.clientId!!,
            )
        }
        return null
    }

    fun WebClient.ResponseSpec.handleStatus() =
        this
            .onStatus(
                { httpStatus -> HttpStatus.NOT_FOUND == httpStatus },
                { throw ResponseStatusException(HttpStatus.NOT_FOUND) },
            ).onStatus({ httpStatus -> HttpStatus.UNAUTHORIZED == httpStatus }, {
                throw ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                )
            })
            .onStatus({ httpStatus -> HttpStatus.INTERNAL_SERVER_ERROR == httpStatus }, {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
            })

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}