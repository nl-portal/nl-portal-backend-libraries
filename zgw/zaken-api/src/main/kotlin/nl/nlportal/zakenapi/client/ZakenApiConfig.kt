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
package nl.nlportal.zakenapi.client

import nl.nlportal.documentenapi.domain.DocumentStatus
import nl.nlportal.documentenapi.domain.DocumentStatus.DEFINITIEF
import nl.nlportal.documentenapi.domain.DocumentStatus.GEARCHIVEERD
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.BEPERKT_OPENBAAR
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.INTERN
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.OPENBAAR
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.ZAAKVERTROUWELIJK
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.UUID

@ConfigurationProperties(prefix = "nl-portal.config.zakenapi")
class ZakenApiConfig {

    var enabled: Boolean = false
    var properties: ZakenApiConfigProperties = ZakenApiConfigProperties()

    class ZakenApiConfigProperties {
        var url: String = ""
        var clientId: String = ""
        var secret: String = ""
        var zaakTypesIdsExcluded: List<UUID> = emptyList()
        var zaakDocumentenConfig: ZaakDocumentenConfig = ZaakDocumentenConfig()
        val useNnpKvkQueryIdentificators: Boolean = false

        class ZaakDocumentenConfig {
            var vertrouwelijkheidsaanduidingWhitelist: List<Vertrouwelijkheid> =
                listOf(
                    OPENBAAR,
                    BEPERKT_OPENBAAR,
                    INTERN,
                    ZAAKVERTROUWELIJK,
                )
            var statusWhitelist: List<DocumentStatus> =
                listOf(
                    DEFINITIEF,
                    GEARCHIVEERD,
                )
        }
    }
}
