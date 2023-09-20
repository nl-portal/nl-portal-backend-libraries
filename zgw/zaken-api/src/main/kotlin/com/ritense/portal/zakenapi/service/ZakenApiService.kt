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
package com.ritense.portal.zakenapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.portal.commonground.authentication.BedrijfAuthentication
import com.ritense.portal.commonground.authentication.BurgerAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.documentenapi.domain.Document
import com.ritense.portal.documentenapi.domain.DocumentStatus
import com.ritense.portal.documentenapi.service.DocumentenApiService
import com.ritense.portal.zakenapi.client.ZakenApiClient
import com.ritense.portal.zakenapi.domain.Zaak
import com.ritense.portal.zakenapi.domain.ZaakDetails
import com.ritense.portal.zakenapi.domain.ZaakDetailsObject
import com.ritense.portal.zakenapi.domain.ZaakDocument
import com.ritense.portal.zakenapi.domain.ZaakObject
import com.ritense.portal.zakenapi.domain.ZaakRol
import com.ritense.portal.zakenapi.domain.ZaakStatus
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import java.util.Locale
import java.util.UUID

class ZakenApiService(
    private val zakenApiClient: ZakenApiClient,
    private val documentenApiService: DocumentenApiService,
    private val objectsApiClient: ObjectsApiClient,
    private val objectMapper: ObjectMapper
) {

    suspend fun getZaken(page: Int, authentication: CommonGroundAuthentication): List<Zaak> {
        return when (authentication) {
            is BurgerAuthentication -> zakenApiClient.getZaken(page, authentication.getBsn())
            // we will need to change this when a better filter becomes available for kvk nummer in zaak list endpoint
            is BedrijfAuthentication -> getZaakRollen(null, authentication.getKvkNummer(), null)
                .map { getZaakFromZaakApi(extractId(it.zaak)) }

            else -> throw IllegalArgumentException("Cannot get zaken for this user")
        }
    }

    suspend fun getZaak(id: UUID, authentication: CommonGroundAuthentication): Zaak {
        val zaak = getZaakFromZaakApi(id)

        // get rollen for zaak based on current user
        val rollen: List<ZaakRol> = when (authentication) {
            is BurgerAuthentication -> getZaakRollen(authentication.getBsn(), null, id)
            is BedrijfAuthentication -> getZaakRollen(null, authentication.getKvkNummer(), id)
            else -> throw IllegalArgumentException("Cannot get zaak for this user")
        }

        // if no rol is found, the current user does not have access to this zaak
        if (rollen.isEmpty())
            throw IllegalStateException("Access denied to this zaak")

        return zaak
    }

    suspend fun getZaakFromZaakApi(id: UUID): Zaak {
        return zakenApiClient.getZaak(id)
    }

    suspend fun getZaakStatus(statusUrl: String): ZaakStatus {
        return zakenApiClient.getStatus(extractId(statusUrl))
    }

    suspend fun getDocumenten(zaakUrl: String): List<Document> {
        return getZaakDocumenten(zaakUrl)
            .map { documentenApiService.getDocument(DocumentenApiService.extractId(it.informatieobject!!)) }
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
        val zaakDetailsObjects = getZaakObjecten(zaakId)
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

    private suspend fun getZaakRollen(bsn: String?, kvknummer: String?, zaakId: UUID?): List<ZaakRol> {
        val rollen = arrayListOf<ZaakRol>()
        var nextPageNumber: Int? = 1

        while (nextPageNumber != null) {
            val zaakRollenPage = zakenApiClient.getZaakRollen(nextPageNumber, bsn, kvknummer, zaakId)
            rollen.addAll(zaakRollenPage.results)
            nextPageNumber = zaakRollenPage.getNextPageNumber()
        }

        return rollen
    }

    private suspend fun getObjectApiZaakDetails(
        objectUrl: String
    ): ObjectsApiObject<ZaakDetailsObject>? {
        return objectsApiClient.getObjectByUrl<ZaakDetailsObject>(
            url = objectUrl
        )
    }

    companion object {
        fun extractId(url: String): UUID {
            return UUID.fromString(url.substringAfterLast("/"))
        }
    }
}