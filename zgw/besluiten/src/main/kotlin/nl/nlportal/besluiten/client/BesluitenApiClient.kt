package nl.nlportal.besluiten.client

import io.netty.handler.logging.LogLevel
import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.domain.BesluitAuditTrail
import nl.nlportal.besluiten.domain.BesluitDocument
import nl.nlportal.besluiten.domain.ResultPage
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.util.UUID

class BesluitenApiClient(
    private val besluitenApiConfig: BesluitenApiConfig,
    webClientBuilder: WebClient.Builder,
) {
    val webClient: WebClient

    init {
        this.webClient =
            webClientBuilder
                .clone()
                .baseUrl(besluitenApiConfig.url)
                .filter(
                    ExchangeFilterFunction.ofRequestProcessor {
                        Mono.just(
                            ClientRequest.from(it).header("Authorization", "Bearer ${getToken()}").build(),
                        )
                    },
                )
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create().wiretap(
                            "reactor.netty.http.client.HttpClient",
                            LogLevel.TRACE,
                            AdvancedByteBufFormat.TEXTUAL,
                        ),
                    ),
                )
                .defaultHeader("Accept-Crs", "EPSG:4326")
                .defaultHeader("Content-Crs", "EPSG:4326")
                .build()
    }

    suspend fun getBesluiten(
        besluitType: String?,
        identificatie: String?,
        page: Int?,
        verantwoordelijkeOrganisatie: String?,
        zaak: String?,
    ): List<Besluit> {
        val params = LinkedMultiValueMap<String, String>()
        besluitType?.let { params.add("besluittype", it) }
        identificatie?.let { params.add("identificatie", it) }
        page?.let { params.add("page", it.toString()) }
        verantwoordelijkeOrganisatie?.let { params.add("verantwoordelijkeOrganisatie", it) }
        zaak?.let { params.add("zaak", it) }
        return webClient
            .get()
            .uri {
                it.path("/besluiten/api/v1/besluiten")
                    .queryParams(params)
                    .build()
            }
            .retrieve()
            .handleStatus()
            .awaitBody<ResultPage<Besluit>>()
            .results
    }

    suspend fun getBesluit(besluitId: UUID): Besluit {
        return webClient
            .get()
            .uri("/besluiten/api/v1/besluiten/$besluitId")
            .retrieve()
            .handleStatus()
            .awaitBody<Besluit>()
    }

    suspend fun getBesluitAuditTrails(besluitId: UUID): List<BesluitAuditTrail> {
        return webClient
            .get()
            .uri("/besluiten/api/v1/besluiten/$besluitId/audittrail")
            .retrieve()
            .handleStatus()
            .awaitBody<List<BesluitAuditTrail>>()
    }

    suspend fun getBesluitAuditTrail(
        besluitId: UUID,
        auditTrail: UUID,
    ): BesluitAuditTrail {
        return webClient
            .get()
            .uri("/besluiten/api/v1/besluiten/$besluitId/audittrail/$auditTrail")
            .retrieve()
            .handleStatus()
            .awaitBody<BesluitAuditTrail>()
    }

    suspend fun getBesluitDocumenten(
        besluit: String?,
        informatieobject: String?,
    ): List<BesluitDocument> {
        val params = LinkedMultiValueMap<String, String>()
        besluit?.let { params.add("besluit", it) }
        informatieobject?.let { params.add("informatieobject", it) }
        return webClient
            .get()
            .uri {
                it.path("/besluiten/api/v1/besluitinformatieobjecten")
                    .queryParams(params)
                    .build()
            }
            .retrieve()
            .handleStatus()
            .awaitBody<List<BesluitDocument>>()
    }

    suspend fun getBesluitDocument(documentId: UUID): BesluitDocument {
        return webClient
            .get()
            .uri("/besluiten/api/v1/besluitinformatieobjecten/$documentId")
            .retrieve()
            .handleStatus()
            .awaitBody<BesluitDocument>()
    }

    private fun getToken(): String {
        return IdTokenGenerator().generateToken(
            besluitenApiConfig.secret,
            besluitenApiConfig.clientId,
        )
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
}