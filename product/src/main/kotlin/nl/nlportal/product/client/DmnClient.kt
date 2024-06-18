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
package nl.nlportal.product.client

import io.netty.handler.logging.LogLevel
import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import nl.nlportal.product.domain.DmnRequestMapping
import nl.nlportal.product.domain.DmnResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.server.ResponseStatusException
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class DmnClient(
    val dmnConfig: DmnConfig,
    private val clientSslContextResolver: ClientSslContextResolver? = null,
    webClientBuilder: WebClient.Builder,
) {
    val webClient: WebClient

    init {
        this.webClient =
            webClientBuilder
                .clone()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create().wiretap(
                            "reactor.netty.http.client.HttpClient",
                            LogLevel.DEBUG,
                            AdvancedByteBufFormat.TEXTUAL,
                        ).let { client ->
                            var result = client
                            if (clientSslContextResolver != null) {
                                dmnConfig.ssl?.let {
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
                .baseUrl(dmnConfig.url)
                .apply {
                    val token = getToken()
                    if (token != null) {
                        it.defaultHeader("Authorization", "Bearer $token")
                    }
                }
                .build()
    }

    suspend fun getDecision(dmnRequestMapping: DmnRequestMapping): DmnResponse {
        return webClient
            .post()
            .uri("/decision-definition/key/${dmnRequestMapping.key}/evaluate")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue(dmnRequestMapping.mapping),
            )
            .retrieve()
            .handleStatus()
            .awaitBody<List<DmnResponse>>()
            .single()
    }

    private fun getToken(): String? {
        if (dmnConfig.clientId != null && dmnConfig.secret != null) {
            return IdTokenGenerator().generateToken(
                dmnConfig.secret,
                dmnConfig.clientId,
            )
        }
        return null
    }

    fun WebClient.ResponseSpec.handleStatus() =
        this
            .onStatus({ httpStatus -> HttpStatus.NOT_FOUND == httpStatus }, { throw ResponseStatusException(HttpStatus.NOT_FOUND) })
            .onStatus({ httpStatus -> HttpStatus.UNAUTHORIZED == httpStatus }, {
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