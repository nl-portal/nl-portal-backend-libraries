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
package nl.nlportal.payment.direct.autoconfiguration

import nl.nlportal.core.util.ShaVersion
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.config.payment.direct.ogone", ignoreUnknownFields = true)
data class OgoneDirectPaymentModuleConfiguration(
    var enabled: Boolean = false,
    var properties: OgonePaymentDirectProperties,
)

data class OgonePaymentDirectProperties(
    var url: String,
    val shaOutParameters: List<String>,
    val configurations: Map<String, OgonePaymentDirectProfile> = mapOf(),
) {
    fun getPaymentProfile(profileIdentifier: String): OgonePaymentDirectProfile? {
        return configurations[profileIdentifier]
    }

    fun getPaymentProfileByPspPid(pspId: String?): OgonePaymentDirectProfile? {
        configurations.forEach {
            if (it.value.pspId == pspId) {
                return it.value
            }
        }
        return null
    }
}

data class OgonePaymentDirectProfile(
    val pspId: String = "",
    val language: String = "nl_NL",
    val currency: String = "EUR",
    val apiKey: String = "",
    val apiSecret: String = "",
    val returnUrl: String = "",
    val shaOutKey: String = "",
    val shaVersion: String = ShaVersion.SHA1.version,
)