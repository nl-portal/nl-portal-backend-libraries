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
package nl.nlportal.zgw.objectenapi.client

import io.netty.handler.logging.LogLevel.TRACE
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import nl.nlportal.core.util.Mapper
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import nl.nlportal.zgw.objectenapi.domain.CreateObjectsApiObjectRequest
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import java.util.UUID

open class ObjectsApiClient(
    private val objectsApiClientConfig: ObjectsApiClientConfig,
) {
    suspend inline fun <reified T> getObjectById(id: String): ObjectsApiObject<T>? {
        return webClient()
            .get()
            .uri("/api/v2/objects/$id")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<ObjectsApiObject<T>>() {})
            .awaitSingleOrNull()
    }

    suspend inline fun <reified T> getObjectByUrl(url: String): ObjectsApiObject<T>? {
        return webClientWithoutBaseUrl()
            .get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<ObjectsApiObject<T>>() {})
            .awaitSingleOrNull()
    }

    suspend inline fun <reified T> getObjects(
        objectSearchParameters: List<ObjectSearchParameter>,
        objectTypeUrl: String? = null,
        page: Int,
        pageSize: Int,
        ordering: String? = null,
    ): ResultPage<ObjectsApiObject<T>> {
        return webClient()
            .get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/v2/objects")
                    .queryParam("page", page)
                    .queryParam("pageSize", pageSize)
                objectTypeUrl?.let { uriBuilder.queryParam("type", it) }
                uriBuilder.queryParam("data_attrs", ObjectSearchParameter.toQueryParameter(objectSearchParameters))
                ordering?.let { uriBuilder.queryParam("ordering", ordering) }
                uriBuilder.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<ResultPage<ObjectsApiObject<T>>>() {})
            .awaitSingle()
    }

    suspend inline fun <reified T> updateObject(
        objectUuid: UUID,
        objectsApiObject: UpdateObjectsApiObjectRequest<T>,
    ): ObjectsApiObject<T> {
        return webClient()
            .put()
            .uri("/api/v2/objects/$objectUuid")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(objectsApiObject)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<ObjectsApiObject<T>>() {})
            .awaitSingle()
    }

    suspend inline fun <reified T> createObject(objectsApiObject: CreateObjectsApiObjectRequest<T>): ObjectsApiObject<T> {
        return webClient()
            .post()
            .uri("/api/v2/objects")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(objectsApiObject)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<ObjectsApiObject<T>>() {})
            .awaitSingle()
    }

    fun webClient(): WebClient {
        return webclientBuilder
            .baseUrl(objectsApiClientConfig.url.toString())
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Token ${objectsApiClientConfig.token}")
            .build()
    }

    fun webClientWithoutBaseUrl(): WebClient {
        return webclientBuilder
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Token ${objectsApiClientConfig.token}")
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