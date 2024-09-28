/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(prefix = "nl-portal.config.openklant")
data class OpenKlantModuleConfiguration(
    var enabled: Boolean = false,
    var properties: OpenKlantConfigurationProperties,
) {
    init {
        if (enabled) {
            requireNotNull(properties.url) {
                "OpenKlant URL not configured"
            }
            requireNotNull(properties.token) {
                "OpenKlant token not configured"
            }
        }
    }

    data class OpenKlantConfigurationProperties(
        var url: URI? = null,
        var token: String? = null,
    )
}