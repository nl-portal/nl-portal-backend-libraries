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

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class BedrijfAuthentication(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>?
) : CommonGroundAuthentication(jwt, authorities) {
    fun getKvkNummer(): String {
        val aanvrager = jwt.claims[AANVRAGER_KEY]
        if (aanvrager is Map<*, *>) {
            return aanvrager[KVK_NUMMER_KEY].toString()
        }

        return jwt.claims[KVK_NUMMER_KEY].toString()
    }

    override fun getUserId() = getKvkNummer()

    override fun getUserRepresentation() = "KVK:${getKvkNummer()}"
}