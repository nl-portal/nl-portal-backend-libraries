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
package nl.nlportal.verificatie.graphql

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.verificatie.graphql.domain.VerificatieCreateInput
import nl.nlportal.verificatie.graphql.domain.VerificatieCreateResponse
import nl.nlportal.verificatie.graphql.domain.VerificatieVerifyInput
import nl.nlportal.verificatie.graphql.domain.VerificatieVerifyResponse
import nl.nlportal.verificatie.service.VerificatieService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class VerificatieMutation(
    val verificatieService: VerificatieService,
) {
    @MutationMapping
    suspend fun createVerificatie(
        @Argument verificatieCreateInput: VerificatieCreateInput,
    ): VerificatieCreateResponse =
        verificatieService.createVerificatie(
            verificatieCreateInput = verificatieCreateInput,
        )

    @MutationMapping
    suspend fun verifyVerificatie(
        authentication: CommonGroundAuthentication,
        @Argument verificatieVerifyInput: VerificatieVerifyInput,
    ): VerificatieVerifyResponse =
        verificatieService.verify(
            verificatieVerifyInput = verificatieVerifyInput,
        )
}