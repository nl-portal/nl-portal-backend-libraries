/*
 * Copyright 2026 Ritense BV, the Netherlands.
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
package nl.nlportal.verificatie.autoconfigure

import java.net.URI
import nl.nlportal.core.ssl.Ssl
import nl.nlportal.verificatie.graphql.domain.VerificatieType
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.config.verificatie")
class VerificatieModuleConfiguration {
    var enabled: Boolean = false
    var properties: VerificatieConfigurationProperties = VerificatieConfigurationProperties()

    init {
        if (enabled) {
            requireNotNull(properties.url) {
                "Verificatie API URL not configured"
            }
        }
    }

    class VerificatieConfigurationProperties(
        var url: URI? = null,
        var clientId: String? = null,
        var secret: String? = null,
        var apiKey: String? = null,
        var templateIdPhoneNumber: String? = null,
        var templateIdEmail: String? = null,
        var ssl: Ssl? = null,
        var typesNeedVerification: List<VerificatieType> = emptyList(),
    )
}