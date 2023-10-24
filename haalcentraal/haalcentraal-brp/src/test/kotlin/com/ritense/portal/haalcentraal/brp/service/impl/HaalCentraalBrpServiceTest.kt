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
package com.ritense.portal.haalcentraal.brp.service.impl

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.portal.commonground.authentication.JwtBuilder
import com.ritense.portal.haalcentraal.brp.domain.persoon.Persoon
import com.ritense.portal.haalcentraal.brp.domain.persoon.PersoonNaam
import com.ritense.portal.haalcentraal.brp.client.HaalCentraalBrpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class HaalCentraalBrpServiceTest {
    val haalCentraalBrpClient = mock<HaalCentraalBrpClient>()
    val haalCentraalBrpService = HaalCentraalBrpService(haalCentraalBrpClient)

    @Test
    fun `getPerson calls client and gets Persoon`() = runBlockingTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        whenever(haalCentraalBrpClient.getPersoon("123", authentication)).thenReturn(
            Persoon(
                "123",
                "geslacht",
                PersoonNaam(
                    "Aanhef",
                    "Voornaam",
                    "V.",
                    "van",
                    "Achternaam",
                ),
                null,
                null,
                null,
            ),
        )

        val persoon = haalCentraalBrpService.getPersoon(authentication)!!

        assertEquals("Achternaam", persoon.naam?.geslachtsnaam)

        verify(haalCentraalBrpClient).getPersoon("123", authentication)
    }

    @Test
    fun `getPerson with invalid bsn`() = runBlockingTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        val persoon = haalCentraalBrpService.getPersoon(authentication)

        assertNull(persoon)
    }

    @Test
    fun `getGemachtigde calls client and gets PersoonNaam`() = runBlockingTest {
        val authentication = JwtBuilder()
            .aanvragerBsn("123")
            .gemachtigdeBsn("456")
            .buildBurgerAuthentication()
        whenever(haalCentraalBrpClient.getPersoonNaam("456", authentication)).thenReturn(
            PersoonNaam(
                "Aanhef",
                "Voornaam",
                "V.",
                "van",
                "Achternaam",
            ),
        )

        val persoonNaam = haalCentraalBrpService.getGemachtigde(authentication)!!

        assertEquals("Achternaam", persoonNaam.geslachtsnaam)

        verify(haalCentraalBrpClient).getPersoonNaam("456", authentication)
    }

    @Test
    fun `getGemachtigde with invalid bsn`() = runBlockingTest {
        val authentication = JwtBuilder()
            .aanvragerBsn("123")
            .gemachtigdeBsn("456")
            .buildBurgerAuthentication()
        val persoonNaam = haalCentraalBrpService.getPersoon(authentication)

        assertNull(persoonNaam)
    }
}