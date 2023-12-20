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
package nl.nlportal.haalcentraal.hr.client

import io.netty.handler.logging.LogLevel
import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.haalcentraal.hr.domain.MaatschappelijkeActiviteit
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class HandelsregisterClient(
    val haalCentraalHrClientConfig: HaalCentraalHrClientConfig,
    val clientSslContextResolver: ClientSslContextResolver? = null,
) {
    suspend fun getMaatschappelijkeActiviteit(kvkNummer: String): MaatschappelijkeActiviteit {
        return webClient()
            .get()
            .uri("/basisprofielen/$kvkNummer")
            .retrieve()
            .awaitBody()
    }

    fun webClient(): WebClient {
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
                            haalCentraalHrClientConfig.ssl?.let {
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
            .baseUrl(haalCentraalHrClientConfig.url)
            .apply {
                if (!haalCentraalHrClientConfig.apiKey.isNullOrBlank()) {
                    it.defaultHeader("X-API-KEY", haalCentraalHrClientConfig.apiKey)
                    logger.debug { "X-API-KEY was set for client" }
                }
            }
            .build()
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}