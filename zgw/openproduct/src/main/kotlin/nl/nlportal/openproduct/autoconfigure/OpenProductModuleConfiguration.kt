/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.autoconfigure

import nl.nlportal.core.ssl.Ssl
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(prefix = "nl-portal.config.openproduct")
class OpenProductModuleConfiguration {
    var enabled: Boolean = false
    var properties: OpenProductConfigurationProperties = OpenProductConfigurationProperties()

    init {
        if (enabled) {
            requireNotNull(properties.productApiUrl) {
                "OpenProduct Product API URL not configured"
            }
            requireNotNull(properties.productTypeApiUrl) {
                "OpenProduct ProductType API URL not configured"
            }
            requireNotNull(properties.token) {
                "OpenProduct token not configured"
            }
        }
    }

    class OpenProductConfigurationProperties {
        var productApiUrl: URI? = null
        var productTypeApiUrl: URI? = null
        var token: String? = null
        var dmn: OpenProductDmnConfigurationProperties = OpenProductDmnConfigurationProperties()

        class OpenProductDmnConfigurationProperties {
            var clientId: String = ""
            var secret: String = ""
            var username: String = ""
            var password: String = ""
            var ssl: Ssl? = null
        }
    }
}