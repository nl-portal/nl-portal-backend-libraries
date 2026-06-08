/*
 * Copyright 2015-2026 Ritense BV, the Netherlands.
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
package nl.nlportal.zakenapi.web.rest

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.zakenapi.service.ZakenApiService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ZaakDocumentResourceTest {
    private val zakenApiService: ZakenApiService = mock()
    private val authentication: CommonGroundAuthentication = mock()
    private val resource = ZaakDocumentResource(zakenApiService)

    @Test
    fun `Content-Disposition header is safely encoded when filename contains CRLF`() {
        val document =
            Document(
                url = "http://example.com/enkelvoudiginformatieobjecten/00000000-0000-0000-0000-000000000001",
                identificatie = "id",
                creatiedatum = "2024-01-01",
                titel = "t",
                formaat = "application/pdf",
                bestandsnaam = "evil\r\nX-Injected: yes.pdf",
                bestandsomvang = 1,
                status = null,
                vertrouwelijkheidaanduiding = null,
            )
        runBlocking {
            whenever(zakenApiService.getZaakDocumentContent("id", authentication))
                .thenReturn(document to emptyFlow())
        }

        val response =
            runBlocking {
                resource.getContentStreaming("id", authentication)
            }

        val header = response.headers.getFirst("Content-Disposition")
        assertNotNull(header)
        assertFalse(header!!.contains('\r'))
        assertFalse(header.contains('\n'))
        assertEquals(200, response.statusCode.value())
    }
}
