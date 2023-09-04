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
package com.ritense.portal.haalcentraal.all.graphql

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import com.ritense.portal.haalcentraal.brp.domain.persoon.PersoonNaam
import com.ritense.portal.haalcentraal.brp.service.HaalCentraalBrpService
import com.ritense.portal.haalcentraal.hr.domain.MaatschappelijkeActiviteit
import com.ritense.portal.haalcentraal.hr.service.HandelsregisterService
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication

@ExperimentalCoroutinesApi
internal class GemachtigdeQueryTest {

    val haalCentraalBrpService = mock<HaalCentraalBrpService>()
    val handelsregisterService = mock<HandelsregisterService>()
    val query = GemachtigdeQuery(haalCentraalBrpService, handelsregisterService)
    var environment = mock(DataFetchingEnvironment::class.java)
    var authentication = mock(CommonGroundAuthentication::class.java)
    val context = mock(GraphQLContext::class.java)

    @BeforeEach
    fun setup() {
        whenever(environment.graphQlContext).thenReturn(context)
        whenever(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
    }

    @Test
    fun `getGemachtigde should call service`() = runBlockingTest {
        whenever(haalCentraalBrpService.getGemachtigde(authentication)).thenReturn(
            PersoonNaam(
                "test",
                "test",
                "test",
                "test",
                "test"
            )
        )
        whenever(handelsregisterService.getGemachtigde(authentication)).thenReturn(
            MaatschappelijkeActiviteit("test")
        )

        val gemachtigde = query.getGemachtigde(environment)
        verify(haalCentraalBrpService).getGemachtigde(authentication)
        verify(handelsregisterService).getGemachtigde(authentication)

        assertEquals("test", gemachtigde.persoon?.aanhef)
        assertEquals("test", gemachtigde.bedrijf?.naam)
    }
}