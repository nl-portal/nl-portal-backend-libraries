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

import com.fasterxml.jackson.core.type.TypeReference
import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.core.util.Mapper
import org.springframework.core.io.ResourceLoader
import java.util.UUID

class AuthenticationMachtigingsDienstService(
    authenticationMachtingsDienstConfig: AuthenticationMachtigingsDienstConfig,
    resourceLoader: ResourceLoader,
) {
    var authenticationMachtingDiensten: List<AuthenticationMachtigingsDienst> = emptyList()

    init {
        try {
            if (authenticationMachtingsDienstConfig.resourceUrl != null) {
                val json = resourceLoader.getResource(authenticationMachtingsDienstConfig.resourceUrl).getContentAsString(Charsets.UTF_8)
                this.authenticationMachtingDiensten = Mapper.get().readValue(json, object : TypeReference<List<AuthenticationMachtigingsDienst>>() {})
            }
        } catch (ex: Exception) {
            logger.warn("Could not load json from {}", authenticationMachtingsDienstConfig.resourceUrl)
        }
    }

    fun getAuthenticationMachtingDienst(uuid: UUID?): AuthenticationMachtigingsDienst? {
        if (uuid == null) return null
        return authenticationMachtingDiensten.find { it.uuid == uuid }
    }

    fun zaakTypes(authentication: CommonGroundAuthentication): List<UUID>? {
        return authentication.machtigingsDienstUUID()?.let {
            getAuthenticationMachtingDienst(it)?.zaakTypes
        }
    }

    fun taakTypes(authentication: CommonGroundAuthentication): List<String>? {
        return authentication.machtigingsDienstUUID()?.let {
            getAuthenticationMachtingDienst(it)?.taakTypes
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}