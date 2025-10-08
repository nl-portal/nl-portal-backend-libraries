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
package nl.nlportal.haalcentraal2.autoconfiguration

import nl.nlportal.core.ssl.Ssl
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.config.haalcentraal2", ignoreUnknownFields = true)
class HaalCentraal2ModuleConfiguration {
    var enabled: Boolean = false
    var properties: HaalCentraal2ConfigurationProperties = HaalCentraal2ConfigurationProperties()

    init {
        if (enabled) {
            requireNotNull(properties.brpApiUrl) {
                "HaalCentraal2 BRP API URL not configured"
            }
        }
    }

    class HaalCentraal2ConfigurationProperties {
        var brpApiUrl: String = ""
        var bewoningApiUrl: String = ""
        var apiKey: String? = null
        var ssl: Ssl? = null
        val brpFields: List<String> =
            listOf(
                "aNummer",
                "adressering",
                "burgerservicenummer",
                "datumEersteInschrijvingGBA",
                "datumInschrijvingInGemeente",
                "europeesKiesrecht",
                "geboorte",
                "gemeenteVanInschrijving",
                "geslacht",
                "gezag",
                "immigratie",
                "indicatieCurateleRegister",
                "kinderen",
                "leeftijd",
                "naam",
                "nationaliteiten",
                "ouders",
                "overlijden",
                "partners",
                "uitsluitingKiesrecht",
                "verblijfplaats",
                "verblijfstitel",
                "verblijfplaatsBinnenland",
                "adresseringBinnenland"
            )
    }
}
