/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.client

import io.netty.handler.logging.LogLevel.TRACE
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties
import nl.nlportal.openklant.client.path.KlantInteractiesPath
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import kotlin.reflect.full.primaryConstructor

class OpenKlant2KlantinteractiesClient(private val openKlantConfigurationProperties: OpenKlantConfigurationProperties) {
    inline fun <reified P : KlantInteractiesPath> path(): P {
        return P::class.primaryConstructor!!.call(this)
    }

    fun webClient(): WebClient {
        return webclientBuilder
            .baseUrl(openKlantConfigurationProperties.klantinteractiesApiUrl.toString())
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Token ${openKlantConfigurationProperties.token}")
            .build()
    }

    private val webclientBuilder =
        WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        TRACE,
                        TEXTUAL,
                    ),
                ),
            )
            .exchangeStrategies(
                ExchangeStrategies.builder()
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
                    }
                    .build(),
            )
}