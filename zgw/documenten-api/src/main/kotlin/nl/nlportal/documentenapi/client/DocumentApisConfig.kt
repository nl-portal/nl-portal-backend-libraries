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
package nl.nlportal.documentenapi.client

import nl.nlportal.core.ssl.Ssl
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.zgw.documentenapis", ignoreUnknownFields = true)
data class DocumentApisConfig(
    var defaultDocumentApi: String,
    var configurations: Map<String, DocumentApiConfig> = mapOf(),
) {
    fun getConfig(documentApi: String): DocumentApiConfig {
        return configurations[documentApi]
            ?: throw NullPointerException("No documentapi configuration with key $documentApi")
    }

    fun getConfigForDocumentUrl(documentUrl: String): String {
        return configurations
            .filterValues { documentenApiConfig ->
                documentUrl.contains(documentenApiConfig.url)
            }
            .keys
            .firstOrNull()
            ?: throw NullPointerException("No documentapi configuration found for zaakdocument with url $documentUrl")
    }
}

data class DocumentApiConfig(
    var url: String,
    var clientId: String? = null,
    var secret: String? = null,
    var rsin: String? = null,
    var documentTypeUrl: String? = null,
    val ssl: Ssl? = null,
)