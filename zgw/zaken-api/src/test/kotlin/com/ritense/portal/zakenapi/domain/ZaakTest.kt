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
package com.ritense.portal.zakenapi.domain

import com.ritense.portal.zakenapi.service.ZakenApiService
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

@ExperimentalCoroutinesApi
internal class ZaakTest {

    var zaakService = mock(ZakenApiService::class.java)

    @Test
    fun status() = runTest {
        val zaak = createTestZaak("test-status")
        zaak.status(zaakService)
        verify(zaakService).getZaakStatus("test-status")
    }

    @Test
    fun `status null`() = runTest {
        val zaak = createTestZaak(null)
        val status = zaak.status(zaakService)
        verifyNoInteractions(zaakService)
        assertNull(status)
    }

    @Test
    fun statusGeschiedenis() = runTest {
        val zaak = createTestZaak()
        zaak.statusGeschiedenis(zaakService)
        verify(zaakService).getZaakStatusHistory(zaak.uuid)
    }

    fun createTestZaak(status: String? = "test-status"): Zaak {
        return Zaak(
            UUID.randomUUID(),
            "http://localhost/zaak",
            "test-identificatie",
            "test-omschrijving",
            "test-zaaktype",
            LocalDate.now(),
            status,
        )
    }
}