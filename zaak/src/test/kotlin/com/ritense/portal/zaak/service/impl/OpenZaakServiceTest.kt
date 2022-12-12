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
package com.ritense.portal.zaak.service.impl

 import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.commonground.authentication.JwtBuilder
import com.ritense.portal.zaak.client.OpenZaakClient
import com.ritense.portal.zaak.client.OpenZaakClientConfig
import com.ritense.portal.zaak.domain.ResultPage
import com.ritense.portal.zaak.domain.catalogi.StatusType
import com.ritense.portal.zaak.domain.documenten.Document
import com.ritense.portal.zaak.domain.documenten.DocumentStatus
import com.ritense.portal.zaak.domain.zaken.ZaakDocument
import com.ritense.portal.zaak.domain.zaken.ZaakRol
import kotlinx.coroutines.ExperimentalCoroutinesApi
 import kotlinx.coroutines.test.runBlockingTest
 import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID

@ExperimentalCoroutinesApi
internal class OpenZaakServiceTest {

    var openZaakClient = mock(OpenZaakClient::class.java)
    var openZaakClientConfig = OpenZaakClientConfig()
    var zaakService = OpenZaakService(openZaakClient, openZaakClientConfig)

    @Test
    fun `getZaken calls openzaak client with BSN for burger`() = runTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        zaakService.getZaken(5, authentication)
        verify(openZaakClient).getZaken(5, "123")
    }

    @Test
    fun `getZaken gets rollen and zaken for rollen for bedrijf`() = runTest {
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()
        val firstZaakId = UUID.randomUUID()
        val secondZaakId = UUID.randomUUID()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/$firstZaakId"),
                    ZaakRol("http://example.com/some-path/$secondZaakId")
                )
            )
        )

        zaakService.getZaken(5, authentication)

        verify(openZaakClient).getZaakRollen(1, null, "123", null)
        verify(openZaakClient).getZaak(firstZaakId)
        verify(openZaakClient).getZaak(secondZaakId)
    }

    @Test
    fun `getZaken throws exception when called with unsupported authentication`() {
        val jwt = Jwt
            .withTokenValue("token")
            .header("alg", "none")
            .claim("random", "1234")
            .build()
        val authentication = object : CommonGroundAuthentication(jwt, emptyList()) {}

        val illegalArgumentException = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runTest {
                zaakService.getZaken(1, authentication)
            }
        }

        assertEquals("Cannot get zaken for this user", illegalArgumentException.message)
    }

    @Test
    fun `getZaak gets zaak and checks rol for burger`() = runTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368")
                )
            )
        )

        zaakService.getZaak(uuid, authentication)

        verify(openZaakClient).getZaak(uuid)
        verify(openZaakClient).getZaakRollen(1, "123", null, uuid)
    }

    @Test
    fun `getZaak gets zaak and checks rol for bedrijf`() = runTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368")
                )
            )
        )

        zaakService.getZaak(uuid, authentication)

        verify(openZaakClient).getZaak(uuid)
        verify(openZaakClient).getZaakRollen(1, null, "123", uuid)
    }

    @Test
    fun `getZaak throws exception when getting zaak user has no rol`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(1, null, null, listOf())
        )

        val illegalStateException = Assertions.assertThrows(IllegalStateException::class.java) {
            runTest {
                zaakService.getZaak(uuid, authentication)
            }
        }

        assertEquals("Access denied to this zaak", illegalStateException.message)
    }

    @Test
    fun `getZaak throws exception when called with unsupported authentication`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val jwt = Jwt
            .withTokenValue("token")
            .header("alg", "none")
            .claim("random", "1234")
            .build()
        val authentication = object : CommonGroundAuthentication(jwt, emptyList()) {}

        val illegalArgumentException = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runTest {
                zaakService.getZaak(uuid, authentication)
            }
        }

        assertEquals("Cannot get zaak for this user", illegalArgumentException.message)
    }

    @Test
    fun getZaakStatus() = runTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakStatus("http://some.domain.com/zaken/api/v1/statussen/$uuid")
        verify(openZaakClient).getStatus(uuid)
    }

    @Test
    fun getZaakStatusHistory() = runTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakStatusHistory(uuid)
        verify(openZaakClient).getStatusHistory(uuid)
    }

    @Test
    fun getZaakStatusType() = runTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakStatusType("http://some.domain.com/catalogi/api/v1/statustypen/$uuid")
        verify(openZaakClient).getStatusType(uuid)
    }

    @Test
    fun getZaakType() = runTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakType("http://some.domain.com/catalogi/api/v1/zaaktypen/$uuid")
        verify(openZaakClient).getZaakType(uuid)
    }

    @Test
    fun `getDocumenten doesnt find in_bewerking document`() = runTest {
        val documenten = getDocumentWithStatus(DocumentStatus.IN_BEWERKING)
        assertEquals(0, documenten.size)
    }

    @Test
    fun `getDocumenten doesnt find ter_vaststelling document`() = runTest {
        val documenten = getDocumentWithStatus(DocumentStatus.TER_VASTSTELLING)
        assertEquals(0, documenten.size)
    }

    @Test
    fun `getDocumenten doesnt find document without status`() = runTest {
        val documenten = getDocumentWithStatus(null)
        assertEquals(0, documenten.size)
    }

    @Test
    fun `getDocumenten finds definitief document`() = runTest {
        val documenten = getDocumentWithStatus(DocumentStatus.DEFINITIEF)
        assertDocumentsReturned(documenten)
    }

    @Test
    fun `getDocumenten finds gearchiveerd document`() = runTest {
        val documenten = getDocumentWithStatus(DocumentStatus.GEARCHIVEERD)
        assertDocumentsReturned(documenten)
    }

    @Test
    fun `getDocument finds document`() = runTest {
        val documentId = UUID.randomUUID()
        `when`(openZaakClient.getDocument(documentId)).thenReturn(
            getTestDocument(null)
        )

        val document = zaakService.getDocument(documentId)

        verify(openZaakClient).getDocument(documentId)

        assertDocumentReturned(document)
    }

    suspend fun getDocumentWithStatus(status: DocumentStatus?): List<Document> {
        val uuid = UUID.randomUUID()
        val zaakUrl = "http://some.domain.com/catalogi/api/v1/zaaktypen/$uuid"

        val documentId = UUID.randomUUID()
        `when`(openZaakClient.getZaakDocumenten(zaakUrl)).thenReturn(
            listOf(ZaakDocument(UUID.randomUUID().toString(), "http://localhost/$documentId"))
        )
        `when`(openZaakClient.getDocument(documentId)).thenReturn(
            getTestDocument(status)
        )

        val documenten = zaakService.getDocumenten(zaakUrl)

        verify(openZaakClient).getZaakDocumenten(zaakUrl)
        verify(openZaakClient).getDocument(documentId)

        return documenten
    }

    fun getTestDocument(status: DocumentStatus?): Document {
        return Document(
            "http://example.com/enkelvoudiginformatieobjecten/0727b025-eaae-4587-a375-3fe671a19dd8",
            "identificatie",
            "2020-04-17",
            "titel",
            "text/plain",
            "bestandsnaam.txt",
            123,
            status
        )
    }

    fun assertDocumentsReturned(documenten: List<Document>) {
        assertEquals(1, documenten.size)
        assertDocumentReturned(documenten.get(0))
    }

    fun assertDocumentReturned(document: Document) {
        assertEquals("http://example.com/enkelvoudiginformatieobjecten/0727b025-eaae-4587-a375-3fe671a19dd8", document.url)
        assertEquals("identificatie", document.identificatie)
        assertEquals("2020-04-17", document.creatiedatum)
        assertEquals("titel", document.titel)
        assertEquals("text/plain", document.formaat)
        assertEquals("bestandsnaam.txt", document.bestandsnaam)
        assertEquals(123, document.bestandsomvang)
    }

    @Test
    fun getZaakStatusTypes() = runTest {
        val uuid = UUID.randomUUID()
        val zaakUrl = "http://some.domain.com/catalogi/api/v1/zaaktypen/$uuid"

        `when`(openZaakClient.getStatusTypes(zaakUrl)).thenReturn(
            listOf(
                StatusType("desc2", false, 2),
                StatusType("desc1", false, 1),
                StatusType("desc3", true, 3)
            )
        )

        val zaakStatusTypes = zaakService.getZaakStatusTypes(zaakUrl)

        assertEquals(3, zaakStatusTypes.size)
        assertEquals("desc1", zaakStatusTypes.get(0).omschrijving)
        assertEquals("desc2", zaakStatusTypes.get(1).omschrijving)
        assertEquals("desc3", zaakStatusTypes.get(2).omschrijving)

        verify(openZaakClient).getStatusTypes(zaakUrl)
    }
}