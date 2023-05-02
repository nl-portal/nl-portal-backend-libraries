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
package com.ritense.portal.klant.client

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.klant.domain.ResultPage
import com.ritense.portal.klant.domain.klanten.Klant
import com.ritense.portal.klant.domain.klanten.KlantCreationRequest
import io.netty.handler.logging.LogLevel
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class OpenKlantClient(
    val openKlantClientConfig: OpenKlantClientConfig,
    val openKlantTokenGenerator: OpenKlantTokenGenerator
) {
    suspend fun getKlanten(authentication: CommonGroundAuthentication, page: Int, bsn: String?): List<Klant> {
        return webClient(authentication)
            .get()
            .uri {
                val uriBuilder = it.path("/klanten/api/v1/klanten")
                    .queryParam("page", page)
                bsn?.let { uriBuilder.queryParam("subjectNatuurlijkPersoon__inpBsn", it) }
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody<ResultPage<Klant>>()
            .results
    }

    suspend fun patchKlant(authentication: CommonGroundAuthentication, klantUrl: String, klant: Klant): Klant {
        return webClient(authentication)
            .patch()
            .uri(klantUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(klant)
            .retrieve()
            .awaitBody()
    }

    suspend fun postKlant(authentication: CommonGroundAuthentication, klant: KlantCreationRequest): Klant {
        return webClient(authentication)
            .post()
            .uri("/klanten/api/v1/klanten")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(klant)
            .retrieve()
            .awaitBody()
    }

    private fun webClient(authentication: CommonGroundAuthentication): WebClient {
        val token = openKlantTokenGenerator.generateToken(
            openKlantClientConfig.secret,
            openKlantClientConfig.clientId,
            authentication
        )

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL
                    )
                )
            )
            .baseUrl(openKlantClientConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}