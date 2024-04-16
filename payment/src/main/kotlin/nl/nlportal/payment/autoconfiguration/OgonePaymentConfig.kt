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
package nl.nlportal.payment.autoconfiguration

import nl.nlportal.payment.constants.ShaVersion
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.payment.ogone", ignoreUnknownFields = true)
data class OgonePaymentConfig(
    val url: String = "https://secure.ogone.com/ncol/prod/orderstandard.asp",
    val taakTypeUrl: String,
    val shaOutParameters: List<String>,
    val configurations: Map<String, OgonePaymentProfile> = mapOf(),
) {
    fun getPaymentProfile(profileIdentifier: String): OgonePaymentProfile? {
        return configurations[profileIdentifier]
    }

    fun getPaymentProfileByPspPid(pspId: String?): OgonePaymentProfile? {
        configurations.forEach {
            if (it.value.pspId == pspId) {
                return it.value
            }
        }
        return null
    }
}

data class OgonePaymentProfile(
    val pspId: String = "",
    val language: String = "nl_NL",
    val currency: String = "EUR",
    val title: String = "",
    val shaInKey: String = "",
    val shaOutKey: String = "",
    val shaVersion: String = ShaVersion.SHA1.version,
    val failureUrl: String = "",
    val successUrl: String = "",
)