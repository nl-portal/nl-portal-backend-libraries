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
package nl.nlportal.commonground.authentication

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.util.UUID

class WithKeycloakUserSecurityContextFactory : WithSecurityContextFactory<WithKeycloakUser> {
    override fun createSecurityContext(bedrijf: WithKeycloakUser): SecurityContext {
        val context: SecurityContext = SecurityContextHolder.createEmptyContext()

        val builder = JwtBuilder().aanvragerUid(UUID.fromString(bedrijf.uuid))
        if (!bedrijf.gemachtigdeBsn.isEmpty()) {
            builder.gemachtigdeBsn(bedrijf.gemachtigdeBsn)
        }
        if (!bedrijf.gemachtigdeKvk.isEmpty()) {
            builder.gemachtigdeKvk(bedrijf.gemachtigdeKvk)
        }
        context.authentication = builder.buildBedrijfAuthentication()

        return context
    }
}