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
package nl.nlportal.zakenapi.client

import nl.nlportal.catalogiapi.client.CatalogiApiConfig
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import nl.nlportal.zakenapi.client.request.ZaakInformatieobjecten
import nl.nlportal.zakenapi.client.request.ZaakObjecten
import nl.nlportal.zakenapi.client.request.ZaakObjectenImpl
import nl.nlportal.zakenapi.client.request.ZaakRollen
import nl.nlportal.zakenapi.client.request.ZaakRollenImpl
import nl.nlportal.zakenapi.client.request.ZaakStatussen
import nl.nlportal.zakenapi.client.request.ZaakStatussenImpl
import nl.nlportal.zakenapi.client.request.Zaken
import nl.nlportal.zakenapi.client.request.ZakenImpl
import nl.nlportal.zakenapi.client.request.ZakenInformatieobjectenImpl
import nl.nlportal.zakenapi.client.request.ZoekenImpl
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.util.UUID

class ZakenApiClient(
    private val zakenApiConfig: ZakenApiConfig,
    private val catalogiApiConfig: CatalogiApiConfig,
    webClientBuilder: WebClient.Builder,
) {
    val webClient: WebClient

    init {
        this.webClient =
            webClientBuilder
                .clone()
                .baseUrl(zakenApiConfig.url)
                .filter(
                    ExchangeFilterFunction.ofRequestProcessor {
                        Mono.just(
                            ClientRequest.from(it).header("Authorization", "Bearer ${getToken()}").build(),
                        )
                    },
                )
                .defaultHeader("Accept-Crs", "EPSG:4326")
                .defaultHeader("Content-Crs", "EPSG:4326")
                .build()
    }

    private fun getToken(): String {
        return IdTokenGenerator().generateToken(
            zakenApiConfig.secret,
            zakenApiConfig.clientId,
        )
    }

    fun getZaakUrl(zaakId: Any): String {
        return "${zakenApiConfig.url}/zaken/api/v1/zaken/$zaakId"
    }

    fun getZaakTypeUrl(zaakTypeId: UUID): String {
        return "${catalogiApiConfig.url}/catalogi/api/v1/zaaktypen/$zaakTypeId"
    }

    fun zaken(): Zaken {
        return ZakenImpl(this)
    }

    fun zoeken(): Zaken {
        return ZoekenImpl(this)
    }

    fun zaakRollen(): ZaakRollen {
        return ZaakRollenImpl(this)
    }

    fun zaakObjecten(): ZaakObjecten {
        return ZaakObjectenImpl(this)
    }

    fun zaakInformatieobjecten(): ZaakInformatieobjecten {
        return ZakenInformatieobjectenImpl(this)
    }

    fun zaakStatussen(): ZaakStatussen {
        return ZaakStatussenImpl(this)
    }
}

fun WebClient.ResponseSpec.handleStatus() =
    this
        .onStatus(
            { httpStatus -> HttpStatus.NOT_FOUND == httpStatus },
            { throw ResponseStatusException(HttpStatus.NOT_FOUND) },
        )
        .onStatus(
            { httpStatus -> HttpStatus.UNAUTHORIZED == httpStatus },
            { throw ResponseStatusException(HttpStatus.UNAUTHORIZED) },
        )
        .onStatus(
            { httpStatus -> HttpStatus.INTERNAL_SERVER_ERROR == httpStatus },
            {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
            },
        )