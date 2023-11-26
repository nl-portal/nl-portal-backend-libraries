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
package nl.nlportal.haalcentraal.hr.service

import nl.nlportal.commonground.authentication.JwtBuilder
import nl.nlportal.haalcentraal.hr.client.HandelsregisterClient
import nl.nlportal.haalcentraal.hr.domain.MaatschappelijkeActiviteit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class HandelsregisterServiceTest {
    val handelsregisterClient = mock<HandelsregisterClient>()
    val handelsregisterService = HandelsregisterService(handelsregisterClient)

    @Test
    fun `getMaatschappelijkeActiviteit calls client and gets MaatschappelijkeActiviteit`() = runTest {
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()
        whenever(handelsregisterClient.getMaatschappelijkeActiviteit("123", authentication)).thenReturn(
            MaatschappelijkeActiviteit("Test bedrijf"),
        )

        val bedrijf = handelsregisterService.getMaatschappelijkeActiviteit(authentication)!!

        assertEquals("Test bedrijf", bedrijf.naam)
        verify(handelsregisterClient).getMaatschappelijkeActiviteit("123", authentication)
    }

    @Test
    fun `getMaatschappelijkeActiviteit with invalid kvk`() = runTest {
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()

        val bedrijf = handelsregisterService.getMaatschappelijkeActiviteit(authentication)

        assertNull(bedrijf)
    }
}