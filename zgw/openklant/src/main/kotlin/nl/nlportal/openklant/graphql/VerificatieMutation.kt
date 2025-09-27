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
package nl.nlportal.openklant.graphql

import com.expediagroup.graphql.server.operations.Mutation
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.openklant.client.domain.VerificatieCreateResponse
import nl.nlportal.openklant.client.domain.VerificatieVerifyResponse
import nl.nlportal.openklant.graphql.domain.VerificatieCreateInput
import nl.nlportal.openklant.graphql.domain.VerificatieVerifyInput
import nl.nlportal.openklant.service.OpenKlantVerificatieService

class VerificatieMutation(
    val verificatieService: OpenKlantVerificatieService,
) : Mutation {
    suspend fun createVerificatie(
        verificatieCreateInput: VerificatieCreateInput,
    ): VerificatieCreateResponse =
        verificatieService.createVerificatie(
            verificatieCreateInput = verificatieCreateInput,
        )

    suspend fun verifyVerificatie(
        dfe: DataFetchingEnvironment,
        verificatieVerifyInput: VerificatieVerifyInput,
    ): VerificatieVerifyResponse =
        verificatieService.verify(
            authentication = dfe.graphQlContext.get(AUTHENTICATION_KEY),
            verificatieVerifyInput = verificatieVerifyInput,
        )
}