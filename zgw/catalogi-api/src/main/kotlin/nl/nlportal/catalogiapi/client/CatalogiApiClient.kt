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
package nl.nlportal.catalogiapi.client

import nl.nlportal.catalogiapi.domain.ResultPage
import nl.nlportal.catalogiapi.domain.StatusType
import nl.nlportal.catalogiapi.domain.ZaakStatusType
import nl.nlportal.catalogiapi.domain.ZaakType
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import io.netty.handler.logging.LogLevel
import nl.nlportal.catalogiapi.domain.BesluitType
import java.util.UUID
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class CatalogiApiClient(
    private val catalogiApiConfig: CatalogiApiConfig,
    private val idTokenGenerator: IdTokenGenerator,
) {
    suspend fun getStatusTypes(zaakType: String): List<StatusType> {
        val params = LinkedMultiValueMap<String, String>()
        params.apply {
            add("zaaktype", zaakType)
            add("status", "definitief")
        }
        return webClient()
            .get()
            .uri {
                it.path("/catalogi/api/v1/statustypen")
                    .queryParams(params)
                    .build()
            }
            .retrieve()
            .awaitBody<ResultPage<StatusType>>()
            .results
    }

    suspend fun getStatusType(statusTypeId: UUID): ZaakStatusType {
        return webClient()
            .get()
            .uri("/catalogi/api/v1/statustypen/$statusTypeId")
            .retrieve()
            .awaitBody()
    }

    suspend fun getZaakType(zaakTypeId: UUID): ZaakType {
        return webClient()
            .get()
            .uri("/catalogi/api/v1/zaaktypen/$zaakTypeId")
            .retrieve()
            .awaitBody()
    }

    suspend fun getBesluitTypes(zaakType: String): List<BesluitType> {
        val params = LinkedMultiValueMap<String, String>()
        params.apply {
            add("zaaktype", zaakType)
        }

        return webClient()
            .get()
            .uri {
                it.path("/catalogi/api/v1/besluittypen")
                    .queryParams(params)
                    .build()
            }
            .retrieve()
            .awaitBody<ResultPage<BesluitType>>()
            .results
    }

    suspend fun getBesluitType(besluitTypeId: UUID): BesluitType {
        return webClient()
            .get()
            .uri("/catalogi/api/v1/besluittypen/$besluitTypeId")
            .retrieve()
            .awaitBody()
    }

    private fun webClient(): WebClient {
        val token =
            idTokenGenerator.generateToken(
                catalogiApiConfig.secret,
                catalogiApiConfig.clientId,
            )

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL,
                    ),
                ),
            )
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
                    .build(),
            )
            .baseUrl(catalogiApiConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}