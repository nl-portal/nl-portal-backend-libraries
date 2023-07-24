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
package com.ritense.portal.zaak.service.impl

import com.ritense.portal.commonground.authentication.BedrijfAuthentication
import com.ritense.portal.commonground.authentication.BurgerAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.zaak.client.OpenZaakClient
import com.ritense.portal.zaak.client.OpenZaakClientConfig
import com.ritense.portal.zaak.domain.catalogi.StatusType
import com.ritense.portal.zaak.domain.catalogi.ZaakStatusType
import com.ritense.portal.zaak.domain.documenten.Document
import com.ritense.portal.zaak.domain.documenten.DocumentContent
import com.ritense.portal.zaak.domain.documenten.DocumentStatus
import com.ritense.portal.zaak.domain.documenten.PostEnkelvoudiginformatieobjectRequest
import com.ritense.portal.zaak.domain.zaken.Zaak
import com.ritense.portal.zaak.domain.zaken.ZaakDocument
import com.ritense.portal.zaak.domain.zaken.ZaakRol
import com.ritense.portal.zaak.domain.zaken.ZaakStatus
import com.ritense.portal.zaak.domain.zaken.ZaakType
import com.ritense.portal.zaak.service.ZaakService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.context.ReactiveSecurityContextHolder

class OpenZaakService(
    val openZaakClient: OpenZaakClient,
    val openZaakClientConfig: OpenZaakClientConfig,
) : ZaakService {

    override suspend fun getZaken(page: Int, authentication: CommonGroundAuthentication): List<Zaak> {
        if (authentication is BurgerAuthentication) {
            return openZaakClient.getZaken(page, authentication.getBsn())
        } else if (authentication is BedrijfAuthentication) {
            // we will need to change this when a better filter becomes available for kvk nummer in zaak list endpoint
            return getZaakRollen(null, authentication.getKvkNummer(), null)
                .map { getZaakFromZaakApi(extractId(it.zaak)) }
        } else {
            throw IllegalArgumentException("Cannot get zaken for this user")
        }
    }

    override suspend fun getZaak(id: UUID, authentication: CommonGroundAuthentication): Zaak {
        val zaak = getZaakFromZaakApi(id)

        // get rollen for zaak based on current user
        val rollen: List<ZaakRol>
        if (authentication is BurgerAuthentication) {
            rollen = getZaakRollen(authentication.getBsn(), null, id)
        } else if (authentication is BedrijfAuthentication) {
            rollen = getZaakRollen(null, authentication.getKvkNummer(), id)
        } else {
            throw IllegalArgumentException("Cannot get zaak for this user")
        }

        // if no rol is found, the current user does not have access to this zaak
        if (rollen.isEmpty())
            throw IllegalStateException("Access denied to this zaak")

        return zaak
    }

    suspend fun getZaakFromZaakApi(id: UUID): Zaak {
        return openZaakClient.getZaak(id)
    }

    override suspend fun getZaakStatus(statusUrl: String): ZaakStatus {
        return openZaakClient.getStatus(extractId(statusUrl))
    }

    override suspend fun getZaakStatusTypes(zaakType: String): List<StatusType> {
        return openZaakClient.getStatusTypes(zaakType).sortedBy { it.volgnummer }
    }

    override suspend fun getZaakStatusHistory(zaakId: UUID): List<ZaakStatus> {
        return openZaakClient.getStatusHistory(zaakId)
    }

    override suspend fun getZaakStatusType(statusTypeUrl: String): ZaakStatusType {
        return openZaakClient.getStatusType(extractId(statusTypeUrl))
    }

    override suspend fun getZaakType(zaakTypeUrl: String): ZaakType {
        return openZaakClient.getZaakType(extractId(zaakTypeUrl))
    }

    override suspend fun getZaakDocumenten(zaakUrl: String): List<ZaakDocument> {
        return openZaakClient.getZaakDocumenten(zaakUrl)
    }

    override suspend fun getDocumenten(zaakUrl: String): List<Document> {
        return getZaakDocumenten(zaakUrl)
            .map { openZaakClient.getDocument(extractId(it.informatieobject!!)) }
            .filter { it.status in listOf(DocumentStatus.DEFINITIEF, DocumentStatus.GEARCHIVEERD) }
    }

    override suspend fun getDocument(documentId: UUID): Document {
        return openZaakClient.getDocument(documentId)
    }

    override suspend fun getDocumentContent(documentId: UUID): DocumentContent {
        val documentContent = openZaakClient.getDocumentContent(documentId)
        return DocumentContent(Base64.getEncoder().encodeToString(documentContent))
    }

    override suspend fun uploadDocument(file: FilePart, documentType: String?): Document {
        val auteur = ReactiveSecurityContextHolder.getContext()
            .map { (it.authentication as CommonGroundAuthentication).getUserId() }
            .awaitSingleOrNull() ?: "valtimo"

        return openZaakClient.postDocument(
            PostEnkelvoudiginformatieobjectRequest(
                bronorganisatie = openZaakClientConfig.rsin,
                creatiedatum = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                titel = file.filename(),
                auteur = auteur,
                status = DocumentStatus.DEFINITIEF,
                taal = "nld",
                bestandsnaam = file.filename(),
                indicatieGebruiksrecht = false,
                informatieobjecttype = documentType ?: openZaakClientConfig.documentTypeUrl,
            ),
            file.content()
        )
    }

    private suspend fun getZaakRollen(bsn: String?, kvknummer: String?, zaakId: UUID?): List<ZaakRol> {
        val rollen = arrayListOf<ZaakRol>()
        var nextPageNumber: Int? = 1

        while (nextPageNumber != null) {
            val zaakRollenPage = openZaakClient.getZaakRollen(nextPageNumber, bsn, kvknummer, zaakId)
            rollen.addAll(zaakRollenPage.results)
            nextPageNumber = zaakRollenPage.getNextPageNumber()
        }

        return rollen
    }

    companion object {
        fun extractId(url: String): UUID {
            return UUID.fromString(url.substringAfterLast("/"))
        }
    }
}