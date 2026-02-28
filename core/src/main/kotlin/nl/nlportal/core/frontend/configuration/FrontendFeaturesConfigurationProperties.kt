/*
 * Copyright 2025 Ritense BV, the Netherlands.
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
package nl.nlportal.core.frontend.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.config.features", ignoreUnknownFields = true)
class FrontendFeaturesConfigurationProperties {
    var myAddressResearchUrl: String = ""
    var myAddressResearchMoreInfoUrl: String = ""
    var myAddressChangeUrl: String = ""
    var myNameChangeUrl: String = ""
    var myGenderChangeUrl: String = ""
    var myBrpChangeUrl: String = ""
    var myBrpConfidentiallyChangeUrl: String = ""
    var themeClass: String = ""
    var messageCountEnabled: Boolean = false
    var messageCountPollingInterval: Int = 0
    var casesPartialSearchEnabled: Boolean = false
    var openProductEnabled: Boolean = false
    var legacyPaymentEnabled: Boolean = false
    var themeApiEnabled: Boolean = false
    var casesResultExplanationEnabled: Boolean = false
}