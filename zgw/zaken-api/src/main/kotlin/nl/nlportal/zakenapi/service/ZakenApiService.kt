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
package nl.nlportal.zakenapi.service

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils.extractId
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.DocumentStatus
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zakenapi.domain.ZaakDetails
import nl.nlportal.zakenapi.domain.ZaakDetailsObject
import nl.nlportal.zakenapi.domain.ZaakDocument
import nl.nlportal.zakenapi.domain.ZaakRol
import nl.nlportal.zakenapi.domain.ZaakStatus
import nl.nlportal.zakenapi.graphql.ZaakPage
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Locale
import java.util.UUID

class ZakenApiService(
    private val zakenApiClient: ZakenApiClient,
    private val documentenApiService: DocumentenApiService,
    private val objectsApiClient: ObjectsApiClient,
) {
    suspend fun getZaken(
        page: Int,
        authentication: CommonGroundAuthentication,
        zaakTypeUrl: String?,
    ): ZaakPage {
        val request =
            zakenApiClient.zaken()
                .search()
                .withAuthentication(authentication)

        zaakTypeUrl?.let { request.ofZaakType(it) }

        return request.retrieve().let {
            ZaakPage.fromResultPage(page, it)
        }
    }

    suspend fun getZaak(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): Zaak {
        // Get rollen of zaak to check if user has access
        val rollen: List<ZaakRol> =
            zakenApiClient.zaakRollen()
                .search()
                .forZaak(id)
                .withAuthentication(authentication)
                .retrieveAll()

        // if no rol is found, the current user does not have access to this zaak
        if (rollen.isEmpty()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied to this zaak")
        }

        return zakenApiClient.zaken().get(id).retrieve()
    }

    suspend fun getZaakFromZaakApi(id: UUID): Zaak {
        return zakenApiClient.zaken().get(id).retrieve()
    }

    suspend fun getZaakStatus(statusUrl: String): ZaakStatus {
        return zakenApiClient.zaakStatussen().get(extractId(statusUrl)).retrieve()
    }

    suspend fun getDocumenten(zaakUrl: String): List<Document> {
        return getZaakDocumenten(zaakUrl)
            .map { documentenApiService.getDocument(it.informatieobject!!) }
            .filter { it.status in listOf(DocumentStatus.DEFINITIEF, DocumentStatus.GEARCHIVEERD) }
    }

    suspend fun getZaakStatusHistory(zaakId: UUID): List<ZaakStatus> {
        return zakenApiClient.zaakStatussen().search().forZaak(zaakId).retrieveAll()
    }

    suspend fun getZaakDocumenten(zaakUrl: String): List<ZaakDocument> {
        return zakenApiClient.zaakInformatieobjecten().search().forZaak(zaakUrl).retrieve()
    }

    suspend fun getZaakDetails(zaakUrl: String): ZaakDetails {
        val zaakId = extractId(zaakUrl)
        val zaakDetailsObjects =
            zakenApiClient.zaakObjecten().search().forZaak(zaakId).retrieveAll()
                .filter { it.objectTypeOverige.lowercase(Locale.getDefault()).contains("zaakdetails") }
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
}