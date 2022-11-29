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
package com.ritense.portal.zaak.client

import com.ritense.portal.core.util.Mapper
import com.ritense.portal.zaak.domain.ResultPage
import com.ritense.portal.zaak.domain.catalogi.StatusType
import com.ritense.portal.zaak.domain.catalogi.ZaakStatusType
import com.ritense.portal.zaak.domain.documenten.Document
import com.ritense.portal.zaak.domain.documenten.PostEnkelvoudiginformatieobjectRequest
import com.ritense.portal.zaak.domain.zaken.Zaak
import com.ritense.portal.zaak.domain.zaken.ZaakDocument
import com.ritense.portal.zaak.domain.zaken.ZaakRol
import com.ritense.portal.zaak.domain.zaken.ZaakStatus
import com.ritense.portal.zaak.domain.zaken.ZaakType
import io.netty.handler.logging.LogLevel
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

class OpenZaakClient(
    val openZaakClientConfig: OpenZaakClientConfig,
    val openZaakTokenGenerator: OpenZaakTokenGenerator
) {
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
            .awaitBody<ResultPage<Zaak>>()
            .results
    }

    suspend fun getZaak(zaakId: UUID): Zaak {
        return webClient()
            .get()
            .uri("/zaken/api/v1/zaken/$zaakId")
            .retrieve()
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
                zaakId?.let { uriBuilder.queryParam("zaak", "${openZaakClientConfig.url}/zaken/api/v1/zaken/$zaakId") }
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody()
    }

    suspend fun getStatus(statusId: UUID): ZaakStatus {
        return webClient()
            .get()
            .uri("/zaken/api/v1/statussen/$statusId")
            .retrieve()
            .awaitBody()
    }

    suspend fun getStatusHistory(zaakId: UUID): List<ZaakStatus> {
        return webClient()
            .get()
            .uri {
                it.path("/zaken/api/v1/statussen")
                    .queryParam("zaak", "${openZaakClientConfig.url}/zaken/api/v1/zaken/$zaakId")
                    .build()
            }
            .retrieve()
            .awaitBody<ResultPage<ZaakStatus>>()
            .results
    }

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

    suspend fun getZaakDocumenten(zaakUrl: String): List<ZaakDocument> {
        return webClient()
            .get()
            .uri {
                it.path("/zaken/api/v1/zaakinformatieobjecten")
                    .queryParam("zaak", zaakUrl)
                    .build()
            }
            .retrieve()
            .awaitBody()
    }

    suspend fun getDocument(id: UUID): Document {
        return webClient()
            .get()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten/$id")
            .retrieve()
            .awaitBody()
    }

    suspend fun getDocumentContent(id: UUID): ByteArray {
        return webClient()
            .get()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten/$id/download")
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .awaitBody()
    }

    fun getDocumentContentStream(id: UUID): Flux<DataBuffer> {
        return webClient()
            .get()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten/$id/download")
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlux(DataBuffer::class.java)
    }

    suspend fun postDocument(
        request: PostEnkelvoudiginformatieobjectRequest,
        documentContent: Flux<DataBuffer>
    ): Document {
        request.inhoud = UUID.randomUUID().toString()
        val (requestPrefix, requestPostfix) = Mapper.get().writeValueAsString(request).split(request.inhoud!!)

        val file = withContext(Dispatchers.IO) {
            Files.createTempFile("tempDocumentUploadRequest", ".json")
        }

        file.writeText(requestPrefix)
        Base64.getEncoder().wrap(file.outputStream(StandardOpenOption.APPEND)).use { base64Output ->
            documentContent
                .map { dataPart -> base64Output.write(dataPart.asInputStream().readBytes()) }
                .blockLast(Duration.ofMinutes(5))
        }
        file.writeText(requestPostfix, Charsets.UTF_8, StandardOpenOption.APPEND)

        val response = webClient()
            .post()
            .uri("/documenten/api/v1/enkelvoudiginformatieobjecten")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromResource(FileSystemResource(file)))
            .retrieve()
            .awaitBody<Document>()

        file.deleteIfExists()
        return response
    }

    private fun webClient(): WebClient {
        val token = openZaakTokenGenerator.generateToken(
            openZaakClientConfig.secret,
            openZaakClientConfig.clientId
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
            .baseUrl(openZaakClientConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}