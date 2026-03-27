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
package nl.nlportal.haalcentraal2.client

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.handler.logging.LogLevel
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.haalcentraal2.autoconfiguration.HaalCentraal2ModuleConfiguration.HaalCentraal2ConfigurationProperties
import nl.nlportal.haalcentraal2.client.path.HaalCentraal2Path
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import kotlin.reflect.full.primaryConstructor

class HaalCentraal2BrpClient(
    val haalCentraal2ConfigurationProperties: HaalCentraal2ConfigurationProperties,
    private val clientSslContextResolver: ClientSslContextResolver? = null,
    webClientBuilder: WebClient.Builder,
) {
    inline fun <reified P : HaalCentraal2Path> path(): P = P::class.primaryConstructor!!.call(this)

    val webClient: WebClient

    init {
        this.webClient =
            webClientBuilder
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
                                    haalCentraal2ConfigurationProperties.ssl?.let {
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
                ).baseUrl(haalCentraal2ConfigurationProperties.brpApiUrl)
                .apply {
                    if (haalCentraal2ConfigurationProperties.additionalHeaders.isNotEmpty()) {
                        it.defaultHeaders { headers ->
                            haalCentraal2ConfigurationProperties.additionalHeaders.forEach { (name, value) ->
                                headers[name] = value
                            }
                        }
                        logger.debug { "Additional default headers were set for client: ${haalCentraal2ConfigurationProperties.additionalHeaders.keys}" }
                    }
                    if (!haalCentraal2ConfigurationProperties.apiKey.isNullOrBlank()) {
                        it.defaultHeader("X-API-KEY", haalCentraal2ConfigurationProperties.apiKey)
                        logger.debug { "X-API-KEY was set for client" }
                    }
                }.build()
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}