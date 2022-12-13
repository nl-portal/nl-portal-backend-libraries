/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.erfpachtdossier.client

import com.ritense.portal.erfpachtdossier.domain.Erfpachtdossier
import com.ritense.portal.erfpachtdossier.domain.Erfpachtdossiers
import io.netty.handler.logging.LogLevel
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class ErfpachtDossierClient(
    val erfpachtDossierClientConfig: ErfpachtDossierClientConfig
        ) {
            suspend fun getDossiers(): Erfpachtdossiers {
                return webClient()
                    .get()
                    .uri {
                        val uriBuilder = it.path("/api/erfpachtdossier")
                        uriBuilder.build()
                    }
                    .retrieve()
                    .awaitBody()
            }
            suspend fun getDossier(dossierId: String): Erfpachtdossier {
                return webClient()
                    .get()
                    .uri {
                        val uriBuilder = it.path("/api/erfpachtdossier/$dossierId")
                        uriBuilder.build()
                    }
                    .retrieve()
                    .awaitBody()
            }

            private fun webClient(): WebClient {
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
            .baseUrl(erfpachtDossierClientConfig.url)
            // TODO This should not be hardcoded
            .defaultHeader("X-Soort-Identiteit", "RelatieID")
            .defaultHeader("X-Nummer-Identiteit", "1234567")
            .build()
    }
}