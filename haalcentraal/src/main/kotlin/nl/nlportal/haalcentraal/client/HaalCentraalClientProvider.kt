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
package nl.nlportal.haalcentraal.client

import nl.nlportal.core.ssl.ClientSslContextResolver
import io.netty.handler.logging.LogLevel
import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.core.Authentication
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class HaalCentraalClientProvider(
    private val haalCentraalClientConfig: HaalCentraalClientConfig,
    private val clientSslContextResolver: ClientSslContextResolver? = null,
) {
    fun webClient(authentication: Authentication): WebClient {
        return WebClient.builder()
            .defaultHeaders {
                // only set jwt from token exchange as bearer token if not available,
                if (it[HttpHeaders.AUTHORIZATION].isNullOrEmpty()) {
                    it.setBearerAuth((authentication as CommonGroundAuthentication).jwt.tokenValue)
                }
            }
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL,
                    ).let { client ->
                        var result = client
                        if (clientSslContextResolver != null) {
                            haalCentraalClientConfig.ssl?.let {
                                val sslContext =
                                    clientSslContextResolver.resolve(
                                        it.key,
                                        it.trustedCertificate,
                                    )

                                result = client.secure { builder -> builder.sslContext(sslContext) }

                                logger.debug {
                                    "Client SSL context was set: private key=${it.key != null}, " +
                                        "trusted certificate=${it.trustedCertificate != null}."
                                }
                            }
                        }
                        result
                    },
                ),
            )
            .baseUrl(haalCentraalClientConfig.url)
            .apply {
                if (!haalCentraalClientConfig.apiKey.isNullOrBlank()) {
                    it.defaultHeader("X-API-KEY", haalCentraalClientConfig.apiKey)
                    logger.debug { "X-API-KEY was set for client" }
                }
            }
            .build()
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val AUTHENTICATION_ATTRIBUTE_NAME = "userAuthentication"
    }
}