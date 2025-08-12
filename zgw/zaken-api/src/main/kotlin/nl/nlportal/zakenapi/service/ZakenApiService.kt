/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
package nl.nlportal.zakenapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import nl.nlportal.commonground.authentication.AuthenticationMachtigingsDienstService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils.extractId
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.ZakenApiConfig.ZakenApiConfigProperties
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zakenapi.domain.ZaakDetails
import nl.nlportal.zakenapi.domain.ZaakDetailsObject
import nl.nlportal.zakenapi.domain.ZaakDocument
import nl.nlportal.zakenapi.domain.ZaakResultaat
import nl.nlportal.zakenapi.domain.ZaakRol
import nl.nlportal.zakenapi.domain.ZaakStatus
import nl.nlportal.zakenapi.domain.ZaakSubStatus
import nl.nlportal.zakenapi.graphql.ZaakPage
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Locale
import java.util.UUID

class ZakenApiService(
    private val zakenApiClient: ZakenApiClient,
    private val zakenApiConfigProperties: ZakenApiConfigProperties,
    private val documentenApiService: DocumentenApiService,
    private val objectsApiClient: ObjectsApiClient,
    private val authenticationMachtigingsDienstService: AuthenticationMachtigingsDienstService,
) {
    suspend fun getZaken(
        page: Int,
        pageSize: Int? = null,
        authentication: CommonGroundAuthentication,
        zaakTypeUrl: String? = null,
        isOpen: Boolean? = null,
        identificatie: String? = null,
        omschrijving: String? = null,
        identificatieContains: String? = null,
    ): ZaakPage {
        val request =
            zakenApiClient.zoeken()
                .search()
                .page(page)
                .withAuthentication(authentication)
                .withRolOmschrijvingGeneriek()

        pageSize?.let { request.pageSize(it) }
        zaakTypeUrl?.let { request.ofZaakType(it) }
        isOpen?.let {
            request.isOpen(isOpen)
        }

        identificatie?.let {
            request.ofIdentificatie(identificatie)
        }

        identificatieContains?.let {
            request.ofIdenfitifactieContains(identificatieContains)
        }

        omschrijving?.let {
            request.ofOmschrijving(it)
        }

        authenticationMachtigingsDienstService.zaakTypes(authentication)?.let {
            request.ofZaakTypes(it)
        }

        if(zakenApiConfigProperties.zaakTypesIdsExcluded.isNotEmpty()) {
            request.notInZaakTypes(zakenApiConfigProperties.zaakTypesIdsExcluded)
        }

        return request.retrieve().let {
            ZaakPage.fromResultPage(page, pageSize ?: 20, it)
        }
    }

    suspend fun getZaak(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): Zaak {
        // Get rollen of zaak to check if user has access
        val zaakRollenRequest =
            zakenApiClient.zaakRollen()
                .search()
                .forZaak(id)
                .withAuthentication(authentication)
                .withOmschrijvingGeneriek()

        val rollen: List<ZaakRol> =
            zaakRollenRequest
                .retrieveAll()

        // if no rol is found, the current user does not have access to this zaak
        if (rollen.isEmpty()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied to this zaak")
        }

        val zaak = zakenApiClient.zaken().get(id).retrieve()

        if (!authenticationMachtigingsDienstService.isAllowedZaakType(authentication, extractId(zaak.zaaktype))) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied to this zaak")
        }

        return zaak
    }

    suspend fun getZaakFromZaakApi(id: UUID): Zaak {
        return zakenApiClient.zaken().get(id).retrieve()
    }

    suspend fun getZaakStatus(statusUrl: String): ZaakStatus {
        return zakenApiClient.zaakStatussen().get(extractId(statusUrl)).retrieve()
    }

    suspend fun getZaakSubStatussen(zaakUrl: String, statusUrl: String): List<ZaakSubStatus> {
        try {
            return zakenApiClient
                .zaakSubStatussen()
                .search()
                .page(1)
                .forZaak(zaakUrl)
                .forStatus(statusUrl)
                .retrieve().results

        } catch (ex: Exception) {
            logger.warn { "Could not get zak sub statussen for $zaakUrl and $statusUrl: $ex" }
        }
        return emptyList()
    }

    suspend fun getDocumenten(zaakUrl: String): List<Document> {
        return getZaakDocumenten(zaakUrl)
            .map { zaakDocument ->
                documentenApiService
                    .getDocument(zaakDocument.informatieobject)
                    .copy(identificatie = zaakDocument.uuid)
            }
            .filter { document ->
                document.status in zakenApiConfigProperties.zaakDocumentenConfig.statusWhitelist &&
                    document.vertrouwelijkheidaanduiding in zakenApiConfigProperties.zaakDocumentenConfig.vertrouwelijkheidsaanduidingWhitelist
            }
    }

    suspend fun getZaakStatusHistory(zaakId: UUID): List<ZaakStatus> {
        return zakenApiClient.zaakStatussen().search().forZaak(zaakId).retrieveAll()
    }

    suspend fun getZaakDocumenten(zaakUrl: String): List<ZaakDocument> {
        return zakenApiClient.zaakInformatieobjecten().search().forZaak(zaakUrl).retrieve()
    }

    suspend fun getZaakResultaat(resultaatUrl: String): ZaakResultaat {
        return zakenApiClient.zaakResultaten().get(extractId(resultaatUrl)).retrieve()
    }

    suspend fun getZaakDetails(zaakUrl: String): ZaakDetails {
        val zaakId = extractId(zaakUrl)
        val zaakDetailsObjects =
            zakenApiClient.zaakObjecten().search().forZaak(zaakId).retrieveAll()
                .filter { it.objectTypeOverige.lowercase(Locale.getDefault()) == "portaalzaakdetails" }
                .map { getObjectApiZaakDetails(it.objectUrl) }
                .map { it?.record?.data?.data!! }
                .flatten()
        return ZaakDetails(zaakUrl, zaakDetailsObjects)
    }

    private suspend fun getObjectApiZaakDetails(objectUrl: String): ObjectsApiObject<ZaakDetailsObject>? {
        return objectsApiClient.getObjectByUrl<ZaakDetailsObject>(
            url = objectUrl,
        )
    }

    suspend fun getZaakDocumentContent(
        zaakDocumentId: String,
        commonGroundAuthentication: CommonGroundAuthentication,
    ): Pair<Document?, Flow<DataBuffer>?> {
        val zaakDocument =
            zakenApiClient
                .zaakInformatieobjecten()
                .get(UUID.fromString(zaakDocumentId))
                .retrieve()

        val zaakRollen =
            zakenApiClient
                .zaakRollen()
                .search()
                .forZaak(zaakDocument.zaak)
                .withAuthentication(commonGroundAuthentication)
                .retrieveAll()

        return when (zaakRollen.isNotEmpty()) {
            true -> {
                documentenApiService
                    .getDocument(zaakDocument.informatieobject) to
                    documentenApiService
                        .getDocumentContentStreaming(zaakDocument.informatieobject)
            }

            else -> null to null
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
