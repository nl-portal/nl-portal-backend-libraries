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
package com.ritense.portal.commonground.authentication

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class BedrijfAuthenticationTest {

    @Test
    fun `getKvkNummer returns kvk nummer from JWT claims`() {
        val jwt = JwtBuilder().aanvragerKvk("1234").buildJwt()

        val authentication = BedrijfAuthentication(jwt, emptyList())

        Assertions.assertEquals("1234", authentication.getKvkNummer())
    }

    @Test
    fun `getKvkNummer returns kvk from JWT claims when using gemachtigde claim`() {
        val jwt = JwtBuilder().aanvragerKvk("1234").gemachtigdeKvk("5678").buildJwt()

        val authentication = BedrijfAuthentication(jwt, emptyList())

        Assertions.assertEquals("1234", authentication.getKvkNummer())
        Assertions.assertEquals("5678", authentication.getGemachtigde()?.kvk)
        Assertions.assertNull(authentication.getGemachtigde()?.bsn)
    }
}