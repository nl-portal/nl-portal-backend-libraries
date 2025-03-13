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
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.core.util.Mapper
import org.springframework.core.io.ResourceLoader
import java.util.UUID

class AuthenticationMachtigingsDienstService(
    val authenticationMachtingsDienstConfig: AuthenticationMachtigingsDienstConfig,
    resourceLoader: ResourceLoader,
) {
    var authenticationMachtingDiensten: List<AuthenticationMachtigingsDienst> = emptyList()

    init {
        try {
            if (authenticationMachtingsDienstConfig.resourceUrl != null) {
                val json = resourceLoader.getResource(authenticationMachtingsDienstConfig.resourceUrl).getContentAsString(Charsets.UTF_8)
                logger.debug { "Machtigingsdiensten is loaded: $json" }
                this.authenticationMachtingDiensten = Mapper.get().readValue(json, object : TypeReference<List<AuthenticationMachtigingsDienst>>() {})
            }
        } catch (ex: Exception) {
            logger.warn { "Could not load json from ${authenticationMachtingsDienstConfig.resourceUrl} with reason ${ex.message}" }
        }
    }

    fun getAuthenticationMachtingDienst(uuid: UUID?): AuthenticationMachtigingsDienst? {
        if (uuid == null) return null
        return authenticationMachtingDiensten.find { it.uuid == uuid }
    }

    fun zaakTypes(authentication: CommonGroundAuthentication): List<UUID>? {
        if (authentication !is BedrijfAuthentication) {
            return null
        }
        val zaakTypeList = mutableListOf<UUID>()
        authentication.machtigingsDienstUUIDs(authenticationMachtingsDienstConfig.allMachtigingUuid)?.forEach {
            val machtigingsDienst = getAuthenticationMachtingDienst(it)
            if (machtigingsDienst != null) {
                zaakTypeList.addAll(machtigingsDienst.zaakTypes)
            }
        }

        return when {
            zaakTypeList.isEmpty() -> {
                null
            }

            else -> zaakTypeList
        }
    }

    fun taakTypes(authentication: CommonGroundAuthentication): List<String>? {
        if (authentication !is BedrijfAuthentication) {
            return null
        }
        val taakTypeList = mutableListOf<String>()
        authentication.machtigingsDienstUUIDs(authenticationMachtingsDienstConfig.allMachtigingUuid)?.forEach {
            val machtigingsDienst = getAuthenticationMachtingDienst(it)
            if (machtigingsDienst != null) {
                taakTypeList.addAll(machtigingsDienst.taakTypes)
            }
        }
        return when {
            taakTypeList.isEmpty() -> {
                null
            }

            else -> taakTypeList
        }
    }

    fun isAllowedZaakType(
        authentication: CommonGroundAuthentication,
        zaakTypeUUID: UUID,
    ): Boolean {
        if (authentication !is BedrijfAuthentication) {
            return true
        }
        val zaaktypes = zaakTypes(authentication)

        if (!zaaktypes.isNullOrEmpty()) {
            return zaaktypes.contains(zaakTypeUUID)
        }

        return true
    }

    fun isAllowedZaakTypes(
        authentication: CommonGroundAuthentication,
        zaakTypeUUIDs: List<UUID>,
    ): Boolean {
        if (authentication !is BedrijfAuthentication) {
            return true
        }
        val zaaktypes = zaakTypes(authentication)

        val allowedZaaktypes = mutableSetOf<UUID>()

        if (!zaaktypes.isNullOrEmpty()) {
            zaakTypeUUIDs.forEach {
                if (zaaktypes.contains(it)) {
                    allowedZaaktypes.add(it)
                }
            }

            return allowedZaaktypes.isNotEmpty()
        }

        return true
    }

    fun isAllowedTaakType(
        authentication: CommonGroundAuthentication,
        taakType: String,
    ): Boolean {
        if (authentication !is BedrijfAuthentication) {
            return true
        }
        val taaktypes = taakTypes(authentication)

        if (!taaktypes.isNullOrEmpty()) {
            return taaktypes.contains(taakType)
        }

        return true
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}