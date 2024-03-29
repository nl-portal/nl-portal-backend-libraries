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

import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
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
import nl.nlportal.zakenapi.domain.ZaakObject
import nl.nlportal.zakenapi.domain.ZaakRol
import nl.nlportal.zakenapi.domain.ZaakStatus
import nl.nlportal.zakenapi.graphql.ZaakPage
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

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
        val resultPage =
            when (authentication) {
                is BurgerAuthentication -> zakenApiClient.getZaken(page, authentication.getBsn(), null, zaakTypeUrl)
                // we will need to change this when a better filter becomes available for kvk nummer in zaak list endpoint
                is BedrijfAuthentication ->
                    zakenApiClient.getZaken(page, null, authentication.getKvkNummer(), zaakTypeUrl)
                else -> throw IllegalArgumentException("Cannot get zaken for this user")
            }

        return resultPage.let {
            ZaakPage.fromResultPage(page, it)
        }
    }

    suspend fun getZaak(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): Zaak {
        // get rollen for zaak based on current user
        val rollen: List<ZaakRol> =
            when (authentication) {
                is BurgerAuthentication -> getZaakRollen(authentication.getBsn(), null, id)
                is BedrijfAuthentication -> getZaakRollen(null, authentication.getKvkNummer(), id)
                else -> throw IllegalArgumentException("Authentication not (yet) supported")
            }

        // if no rol is found, the current user does not have access to this zaak
        if (rollen.isEmpty()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied to this zaak")
        }

        return getZaakFromZaakApi(id)
    }

    suspend fun getZaakFromZaakApi(id: UUID): Zaak {
        return zakenApiClient.getZaak(id)
    }

    suspend fun getZaakStatus(statusUrl: String): ZaakStatus {
        return zakenApiClient.getStatus(extractId(statusUrl))
    }

    suspend fun getDocumenten(zaakUrl: String): List<Document> {
        return getZaakDocumenten(zaakUrl)
            .map { documentenApiService.getDocument(it.informatieobject!!) }
            .filter { it.status in listOf(DocumentStatus.DEFINITIEF, DocumentStatus.GEARCHIVEERD) }
    }

    suspend fun getZaakStatusHistory(zaakId: UUID): List<ZaakStatus> {
        return zakenApiClient.getStatusHistory(zaakId)
    }

    suspend fun getZaakDocumenten(zaakUrl: String): List<ZaakDocument> {
        return zakenApiClient.getZaakDocumenten(zaakUrl)
    }

    suspend fun getZaakDetails(zaakUrl: String): ZaakDetails {
        val zaakId = extractId(zaakUrl)
        val zaakDetailsObjects =
            getZaakObjecten(zaakId)
                .filter { it.objectTypeOverige.lowercase(Locale.getDefault()).contains("zaakdetails") }
                .map { getObjectApiZaakDetails(it.objectUrl) }
                .map { it?.record?.data?.data!! }
                .flatten()
        return ZaakDetails(zaakUrl, zaakDetailsObjects)
    }

    suspend fun getZaakObjecten(zaakId: UUID?): List<ZaakObject> {
        val zaakObjecten = arrayListOf<ZaakObject>()
        var nextPageNumber: Int? = 1

        while (nextPageNumber != null) {
            val zaakObjectenPage = zakenApiClient.getZaakObjecten(nextPageNumber, zaakId)
            zaakObjecten.addAll(zaakObjectenPage.results)
            nextPageNumber = zaakObjectenPage.getNextPageNumber()
        }

        return zaakObjecten
    }

    private suspend fun getZaakRollen(
        bsn: String?,
        kvknummer: String?,
        zaakId: UUID?,
    ): List<ZaakRol> {
        val rollen = arrayListOf<ZaakRol>()
        var nextPageNumber: Int? = 1

        while (nextPageNumber != null) {
            val zaakRollenPage = zakenApiClient.getZaakRollen(nextPageNumber, bsn, kvknummer, zaakId)
            rollen.addAll(zaakRollenPage.results)
            nextPageNumber = zaakRollenPage.getNextPageNumber()
        }

        return rollen
    }

    private suspend fun getObjectApiZaakDetails(objectUrl: String): ObjectsApiObject<ZaakDetailsObject>? {
        return objectsApiClient.getObjectByUrl<ZaakDetailsObject>(
            url = objectUrl,
        )
    }
}