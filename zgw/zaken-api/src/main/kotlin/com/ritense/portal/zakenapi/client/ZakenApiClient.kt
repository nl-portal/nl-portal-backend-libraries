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
package com.ritense.portal.zakenapi.client

import com.ritense.portal.idtokenauthentication.service.IdTokenGenerator
import com.ritense.portal.zakenapi.domain.ResultPage
import com.ritense.portal.zakenapi.domain.Zaak
import com.ritense.portal.zakenapi.domain.ZaakDocument
import com.ritense.portal.zakenapi.domain.ZaakObject
import com.ritense.portal.zakenapi.domain.ZaakRol
import com.ritense.portal.zakenapi.domain.ZaakStatus
import io.netty.handler.logging.LogLevel
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.server.ResponseStatusException
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.util.*

class ZakenApiClient(
    private val zakenApiConfig: ZakenApiConfig,
    private val idTokenGenerator: IdTokenGenerator
) {
    private fun WebClient.ResponseSpec.handleStatus() = this
        .onStatus({ httpStatus -> HttpStatus.NOT_FOUND == httpStatus }, { throw ResponseStatusException(HttpStatus.NOT_FOUND) })
        .onStatus({ httpStatus -> HttpStatus.UNAUTHORIZED == httpStatus }, { throw ResponseStatusException(HttpStatus.UNAUTHORIZED) })
        .onStatus({ httpStatus -> HttpStatus.INTERNAL_SERVER_ERROR == httpStatus }, { throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR) })

    suspend fun getZaken(page: Int, bsn: String?): List<Zaak> {
        return webClient()
            .get()
            .uri {
                val uriBuilder = it.path("/zaken/api/v1/zaken")
                    .queryParam("page", page)
                bsn?.let { uriBuilder.queryParam("rol__betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", it) }
                uriBuilder.build()
            }
            .retrieve()
            .handleStatus()
            .awaitBody<ResultPage<Zaak>>()
            .results
    }

    suspend fun getZaak(zaakId: UUID): Zaak {
        return webClient()
            .get()
            .uri("/zaken/api/v1/zaken/$zaakId")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }

    suspend fun getZaakRollen(page: Int, bsn: String?, kvknummer: String?, zaakId: UUID?): ResultPage<ZaakRol> {
        return webClient()
            .get()
            .uri {
                val uriBuilder = it.path("/zaken/api/v1/rollen")
                    .queryParam("page", page)
                bsn?.let { uriBuilder.queryParam("betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", it) }
                kvknummer?.let { uriBuilder.queryParam("betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie", it) }
                zaakId?.let { uriBuilder.queryParam("zaak", "${zakenApiConfig.url}/zaken/api/v1/zaken/$zaakId") }
                uriBuilder.build()
            }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }

    suspend fun getZaakObjecten(page: Int, zaakId: UUID?): ResultPage<ZaakObject> {
        return webClient()
            .get()
            .uri {
                val uriBuilder = it.path("/zaken/api/v1/zaakobjecten")
                    .queryParam("page", page)
                    .queryParam("objectType", "overige")
                zaakId?.let { uriBuilder.queryParam("zaak", "${zakenApiConfig.url}/zaken/api/v1/zaken/$zaakId") }
                uriBuilder.build()
            }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }

    suspend fun getStatus(statusId: UUID): ZaakStatus {
        return webClient()
            .get()
            .uri("/zaken/api/v1/statussen/$statusId")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }

    suspend fun getStatusHistory(zaakId: UUID): List<ZaakStatus> {
        return webClient()
            .get()
            .uri {
                it.path("/zaken/api/v1/statussen")
                    .queryParam("zaak", "${zakenApiConfig.url}/zaken/api/v1/zaken/$zaakId")
                    .build()
            }
            .retrieve()
            .handleStatus()
            .awaitBody<ResultPage<ZaakStatus>>()
            .results
    }

    suspend fun getZaakDocumenten(zaakUrl: String): List<ZaakDocument> {
        return webClient()
            .get()
            .uri {
                it.path("/zaken/api/v1/zaakinformatieobjecten")
                    .queryParam("zaak", zaakUrl)
                    .build()
            }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }

    private fun webClient(): WebClient {
        val token = idTokenGenerator.generateToken(
            zakenApiConfig.secret,
            zakenApiConfig.clientId
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
            .baseUrl(zakenApiConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}