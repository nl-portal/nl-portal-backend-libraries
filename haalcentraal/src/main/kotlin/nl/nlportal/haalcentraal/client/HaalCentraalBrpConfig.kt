/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.haalcentraal.client

import nl.nlportal.core.ssl.Ssl
import org.springframework.boot.context.properties.ConfigurationProperties

@Deprecated("Will be removed in 3.0.0, use haalcentraal 2")
@ConfigurationProperties(prefix = "nl-portal.config.haalcentraal.brp", ignoreUnknownFields = true)
data class HaalCentraalBrpConfig(
    var enabled: Boolean = false,
    var properties: HaalCentraalBrpConfigProperties = HaalCentraalBrpConfigProperties(),
) {
    data class HaalCentraalBrpConfigProperties(
        var url: String = "",
        var apiKey: String? = null,
        var ssl: Ssl? = null,
    )
}