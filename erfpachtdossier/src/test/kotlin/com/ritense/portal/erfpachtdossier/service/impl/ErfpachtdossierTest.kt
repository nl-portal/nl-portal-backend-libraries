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
package com.ritense.portal.erfpachtdossier.service.impl

import com.ritense.portal.commonground.authentication.JwtBuilder
import com.ritense.portal.erfpachtdossier.client.ErfpachtDossierClient
import com.ritense.portal.erfpachtdossier.client.ErfpachtDossierClientConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
internal class ErfpachtdossierTest {
    var erfpachtDossierClient = mock(ErfpachtDossierClient::class.java)
    var erfpachtDossierClientConfig = ErfpachtDossierClientConfig()
    var erfpachtDossierService = ErfpachtDossierService(erfpachtDossierClient)

    @Test
    fun `getDossier calls erfpachtdossier client with BSN for burger`() = runTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        erfpachtDossierService.getDossiers(authentication)
        verify(erfpachtDossierClient).getDossiers()
    }
}