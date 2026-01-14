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
package nl.nlportal.verificatie.graphql.domain

import java.time.ZonedDateTime
import java.util.UUID
import nl.nlportal.verificatie.graphql.VerificatieType

data class VerificatieCreateInput(
    val uuid: UUID? = null,
    val waarde: String,
    val type: VerificatieType,
)

data class VerificatieVerifyInput(
    val uuid: UUID? = null,
    val waarde: String,
    val type: VerificatieType,
    val code: String,
)

data class VerificatieCreateResponse(
    val uuid: UUID? = null,
    val success: Boolean,
    val errorMessage: String? = null,
)

data class VerificatieVerifyResponse(
    val uuid: UUID? = null,
    val verified: Boolean,
    val verifiedOp: ZonedDateTime,
    val errorMessage: String? = null,
)