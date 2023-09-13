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
package com.ritense.portal.haalcentraal.client

import com.ritense.portal.core.ssl.ClientSslContextResolver
import com.ritense.portal.haalcentraal.client.tokenexchange.UserTokenExchangeFilter
import io.netty.handler.logging.LogLevel
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.core.Authentication
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class HaalCentraalClientProvider(
    private val haalCentraalClientConfig: HaalCentraalClientConfig,
    private val clientSslContextResolver: ClientSslContextResolver? = null,
    private val userTokenExchangeFilter: UserTokenExchangeFilter? = null
) {
    fun webClient(authentication: Authentication): WebClient {
        return WebClient.builder()
            .defaultRequest { spec ->
                spec.attribute(AUTHENTICATION_ATTRIBUTE_NAME, authentication)
            }
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL
                    ).let { client ->
                        var result = client
                        if (clientSslContextResolver != null) {
                            haalCentraalClientConfig.ssl?.let {
                                val sslContext = clientSslContextResolver.resolve(
                                    it.key,
                                    it.trustedCertificate
                                )

                                result = client.secure { builder -> builder.sslContext(sslContext) }

                                logger.debug { "Client SSL context was set: private key=${it.key != null}, trusted certificate=${it.trustedCertificate != null}." }
                            }
                        }
                        result
                    }
                )
            )
            .baseUrl(haalCentraalClientConfig.url)
            .apply {
                if (!haalCentraalClientConfig.apiKey.isNullOrBlank()) {
                    it.defaultHeader("X-API-KEY", haalCentraalClientConfig.apiKey)
                    logger.debug { "X-API-KEY was set for client" }
                }

                if (haalCentraalClientConfig.tokenExchange != null) {
                    requireNotNull(userTokenExchangeFilter) {
                        "Token exchange was configured (${haalCentraalClientConfig.tokenExchange}), but userTokenExchangeFilter was null!"
                    }

                    it.filter(userTokenExchangeFilter)
                }
            }
            .build()
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val AUTHENTICATION_ATTRIBUTE_NAME = "userAuthentication"
    }
}