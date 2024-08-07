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
package nl.nlportal.documentenapi.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class Document(
    @GraphQLIgnore
    val url: String?,
    var identificatie: String?,
    val creatiedatum: String?,
    val titel: String?,
    val formaat: String?,
    val bestandsnaam: String?,
    val bestandsomvang: Int?,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GraphQLIgnore
    val status: DocumentStatus?,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GraphQLIgnore
    val vertrouwelijkheidaanduiding: Vertrouwelijkheid?,
    var documentapi: String = "",
) {
    val uuid: UUID
        get() {
            if (url.isNullOrBlank()) {
                throw IllegalStateException("No Document url found")
            }
            return UUID.fromString(url.substringAfterLast('/'))
        }
}