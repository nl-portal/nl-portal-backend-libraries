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
package com.ritense.portal.zaak.service

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.zaak.domain.documenten.Document
import com.ritense.portal.zaak.domain.documenten.DocumentContent
import com.ritense.portal.zaak.domain.zaken.Zaak
import com.ritense.portal.zaak.domain.zaken.ZaakDocument
import com.ritense.portal.zaak.domain.zaken.ZaakStatus
import com.ritense.portal.zaak.domain.catalogi.ZaakStatusType
import com.ritense.portal.zaak.domain.zaken.ZaakType
import com.ritense.portal.zaak.domain.catalogi.StatusType
import java.util.UUID
import org.springframework.http.codec.multipart.FilePart

interface ZaakService {
    suspend fun getZaken(page: Int, authentication: CommonGroundAuthentication): List<Zaak>

    suspend fun getZaak(id: UUID, authentication: CommonGroundAuthentication): Zaak

    suspend fun getZaakStatus(statusUrl: String): ZaakStatus

    suspend fun getZaakStatusTypes(zaakType: String): List<StatusType>

    suspend fun getZaakStatusHistory(zaakId: UUID): List<ZaakStatus>

    suspend fun getZaakStatusType(statusTypeUrl: String): ZaakStatusType

    suspend fun getZaakType(zaakTypeUrl: String): ZaakType

    suspend fun getZaakDocumenten(zaakUrl: String): List<ZaakDocument>

    suspend fun getDocumenten(zaakUrl: String): List<Document>

    suspend fun getDocument(documentId: UUID): Document

    suspend fun getDocumentContent(documentId: UUID): DocumentContent

    suspend fun uploadDocument(file: FilePart): Document
}