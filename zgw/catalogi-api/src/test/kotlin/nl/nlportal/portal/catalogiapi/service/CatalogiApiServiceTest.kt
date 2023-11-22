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
package nl.nlportal.portal.catalogiapi.service

import nl.nlportal.catalogiapi.client.CatalogiApiClient
import nl.nlportal.catalogiapi.domain.StatusType
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.nlportal.catalogiapi.service.CatalogiApiService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
internal class CatalogiApiServiceTest {

    var catalogiApiClient = mock(CatalogiApiClient::class.java)
    var catalogiApiService = CatalogiApiService(catalogiApiClient)

    @Test
    fun getZaakStatusType() = runTest {
        val uuid = UUID.randomUUID()
        catalogiApiService.getZaakStatusType("http://some.domain.com/catalogi/api/v1/statustypen/$uuid")
        verify(catalogiApiClient).getStatusType(uuid)
    }

    @Test
    fun getZaakType() = runTest {
        val uuid = UUID.randomUUID()
        catalogiApiService.getZaakType("http://some.domain.com/catalogi/api/v1/zaaktypen/$uuid")
        verify(catalogiApiClient).getZaakType(uuid)
    }

    @Test
    fun getZaakStatusTypes() = runTest {
        val uuid = UUID.randomUUID()
        val zaakUrl = "http://some.domain.com/catalogi/api/v1/zaaktypen/$uuid"

        `when`(catalogiApiClient.getStatusTypes(zaakUrl)).thenReturn(
            listOf(
                StatusType("desc2", false, 2),
                StatusType("desc1", false, 1),
                StatusType("desc3", true, 3),
            ),
        )

        val zaakStatusTypes = catalogiApiService.getZaakStatusTypes(zaakUrl)

        assertEquals(3, zaakStatusTypes.size)
        assertEquals("desc1", zaakStatusTypes.get(0).omschrijving)
        assertEquals("desc2", zaakStatusTypes.get(1).omschrijving)
        assertEquals("desc3", zaakStatusTypes.get(2).omschrijving)

        verify(catalogiApiClient).getStatusTypes(zaakUrl)
    }
}