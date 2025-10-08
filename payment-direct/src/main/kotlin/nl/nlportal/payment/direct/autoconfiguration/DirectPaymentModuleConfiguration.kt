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

import nl.nlportal.core.ssl.Ssl
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.config.payment.direct", ignoreUnknownFields = true)
class DirectPaymentModuleConfiguration(
    var enabled: Boolean = false,
    var properties: DirectPaymentProperties = DirectPaymentProperties(),
) {
    class DirectPaymentProperties(
        var url: String = "",
        var ssl: Ssl? = null,
        var shaOutParameters: List<String> = emptyList(),
        var webhookHeaders: List<String> = emptyList(),
        var webhookUrl: String? = null,
        var customTemplateUrl: String? = null,
        var showResultPage: Boolean = false,
        var configurations: Map<String, DirectPaymentProfile> = emptyMap(),
    ) {
        fun getPaymentProfile(profileIdentifier: String): DirectPaymentProfile? = configurations[profileIdentifier]

        fun getPaymentProfileByPspPid(pspId: String?): DirectPaymentProfile? {
            configurations.forEach {
                if (it.value.pspId == pspId) {
                    return it.value
                }
            }
            return null
        }
    }

    class DirectPaymentProfile(
        var pspId: String = "",
        var language: String = "nl_NL",
        var currency: String = "EUR",
        var apiKey: String = "",
        var apiSecret: String = "",
        var returnUrl: String = "",
        var webhookApiKey: String = "",
        var webhookApiSecret: String = "",
    )
}