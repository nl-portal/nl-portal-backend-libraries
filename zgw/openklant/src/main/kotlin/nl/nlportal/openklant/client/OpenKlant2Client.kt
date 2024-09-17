/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties
import nl.nlportal.openklant.domain.CreatePartij
import nl.nlportal.openklant.domain.Partij
import nl.nlportal.openklant.domain.ResultPage
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL

class OpenKlant2Client(private val openKlantConfigurationProperties: OpenKlantConfigurationProperties) {
    suspend fun findPartij(authentication: CommonGroundAuthentication): Partij? {
        val soortPartij = when (authentication) {
            is BurgerAuthentication -> "persoon"
            is BedrijfAuthentication -> "organisatie"
            else -> "contactpersoon"
        }
        val searchVariables = mapOf(
            "soortPartij" to authentication.asSoortPartij(),
            "partijIdentificator__objectId" to authentication.userId
        )

        val response: ResultPage<Partij> = webClient()
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/partijen")
                    .build(searchVariables)
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody()

        return response.results.singleOrNull()
    }

    suspend fun createPartij(partij: CreatePartij, authentication: CommonGroundAuthentication): Partij? {
        val response: ResultPage<Partij> = webClient()
            .post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/partijen")
                    .build()
            }
            .body(BodyInserters.fromValue(partij))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody()

        return response.results.singleOrNull()
    }


    suspend fun putPartij(partij: Partij, authentication: CommonGroundAuthentication): Partij? {
        val response: ResultPage<Partij> = webClient()
            .put()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/partijen/${partij.uuid}")
                    .build()
            }
            .body(BodyInserters.fromValue(partij))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody()

        return response.results.singleOrNull()
    }

    private fun CommonGroundAuthentication.asSoortPartij(): String {
        return when (this) {
            is BurgerAuthentication -> "persoon"
            is BedrijfAuthentication -> "organisatie"
            else -> "contactpersoon"
        }
    }

    fun webClient(): WebClient {
        return webclientBuilder
            .baseUrl(openKlantConfigurationProperties.url.toString())
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Token ${openKlantConfigurationProperties.token}")
            .build()
    }

    fun webClientWithoutBaseUrl(): WebClient {
        return webclientBuilder
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