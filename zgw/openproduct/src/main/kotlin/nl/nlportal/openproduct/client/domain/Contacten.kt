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
package nl.nlportal.openproduct.client.domain

import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class OpenProductContact(
    val uuid: UUID,
    val naam: String,
    val email: String? = null,
    val telefoonnummer: String? = null,
    val straat: String? = null,
    val huisnummer: String? = null,
    val postcode: String? = null,
    val stad: String? = null,
    val organisatie: OpenProductOrganisatie? = null,
)

enum class OpenProductContactenFilters(
    @JsonValue val value: String,
) : OpenProductFilters {
    PAGE("page"),
    PAGE_SIZE("page_size"),
    NAAM("naam"),
    EMAIL_EXACT("email__iexact"),
    ORGANISATIE_NAAM("organisatie__naam"),
    ORGANISATIE_UUID("organisatie__uui"),
    ROL("rol"),
    TELEFOONNUMMER_CONTAINS("telefoonnummer__contains"),
    VOORNNAAM_CONTAINS("voornaam"),
    ;

    override fun toString() = this.value
}