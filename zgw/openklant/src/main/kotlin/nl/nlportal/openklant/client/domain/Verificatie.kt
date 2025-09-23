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
package nl.nlportal.openklant.client.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class VerificatieCreateRequest(
    val email: String? = null,
    val phoneNumber: String? = null,
    val reference: String? = null,
    val apiKey: String? = null,
    val templateId: String? = null,
)

data class VerificatieCreateResponse(
 val success: Boolean,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VerificatieVerifyRequest(
    val code: String,
    val reference: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,

)

data class VerificatieVerifyResponse(
    val verified: Boolean,
)
