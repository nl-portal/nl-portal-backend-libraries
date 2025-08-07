/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.client

import io.netty.handler.logging.LogLevel
import nl.nlportal.core.util.Mapper
import nl.nlportal.openproduct.autoconfigure.OpenProductModuleConfiguration.OpenProductConfigurationProperties
import nl.nlportal.openproduct.client.path.OpenProductPath
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import kotlin.reflect.full.primaryConstructor

class OpenProductClient(
    val openProductConfigurationProperties: OpenProductConfigurationProperties,
) {
    inline fun <reified P : OpenProductPath> path(): P = P::class.primaryConstructor!!.call(this)

    val webClient: WebClient

    init {
        this.webClient =
            WebClient
                .builder()
                .clone()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create().wiretap(
                            "reactor.netty.http.client.HttpClient",
                            LogLevel.TRACE,
                            AdvancedByteBufFormat.TEXTUAL,
                        ),
                    ),
                ).baseUrl(openProductConfigurationProperties.productApiUrl.toString())
                .apply {
                    it.defaultHeader("Authorization", "Token ${openProductConfigurationProperties.token}")
                    it.defaultHeader("Accept-Crs", "EPSG:4326")
                    it.defaultHeader("Content-Crs", "EPSG:4326")
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
                ).build()
    }
}