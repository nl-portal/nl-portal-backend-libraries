/*
 * Copyright 2024 Ritense BV, the Netherlands.
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

import java.time.LocalDate
import java.util.UUID
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.graphql.domain.DigitaleAdresRequest
import nl.nlportal.openklant.graphql.domain.DigitaleAdresResponse
import nl.nlportal.openklant.graphql.domain.DigitaleAdresType
import nl.nlportal.openklant.service.OpenKlant2Service
import nl.nlportal.verificatie.autoconfigure.VerificatieModuleConfiguration
import nl.nlportal.verificatie.graphql.domain.VerificatieCreateInput
import nl.nlportal.verificatie.graphql.domain.VerificatieType
import nl.nlportal.verificatie.graphql.domain.VerificatieVerifyInput
import nl.nlportal.verificatie.service.VerificatieService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.server.ResponseStatusException

@Controller
class DigitaleAdresMutation(
    private val openklant2Service: OpenKlant2Service,
    private val verificatieModuleConfiguration: VerificatieModuleConfiguration,
    private val verificatieService: VerificatieService?,
) {
    @MutationMapping
    suspend fun createUserDigitaleAdres(
        authentication: CommonGroundAuthentication,
        @Argument digitaleAdresRequest: DigitaleAdresRequest,
    ): DigitaleAdresResponse? {
        if (verificatieService != null &&
            verificatieModuleConfiguration.properties.typesNeedVerification.contains(DigitaleAdresType.toVerificationType(digitaleAdresRequest.type))
        ) {
            // verificate flow
            val response = doVerificatie(digitaleAdresRequest)
            if (response != null) return response

            digitaleAdresRequest.verificatieDatum = LocalDate.now()
        }

        val digitaleAdres =
            openklant2Service.createDigitaleAdres(
                authentication = authentication,
                digitaleAdres = digitaleAdresRequest.asOpenKlant2DigitaleAdres(),
            )
        return digitaleAdres?.let { DigitaleAdresResponse.fromOpenKlant2DigitaleAdres(digitaleAdres, verificatieModuleConfiguration, verificatieService) }
    }

    @MutationMapping
    suspend fun updateUserDigitaleAdres(
        authentication: CommonGroundAuthentication,
        @Argument digitaleAdresRequest: DigitaleAdresRequest,
    ): DigitaleAdresResponse? {
        if (verificatieService != null &&
            verificatieModuleConfiguration.properties.typesNeedVerification.contains(DigitaleAdresType.toVerificationType(digitaleAdresRequest.type)) &&
            digitaleAdresRequest.verificatieDatum == null
        ) {
            // verificate flow
            val response = doVerificatie(digitaleAdresRequest)
            if (response != null) return response
            digitaleAdresRequest.verificatieDatum = LocalDate.now()
        }
        val digitaleAdres =
            openklant2Service
                .updateDigitaleAdresById(
                    authentication = authentication,
                    digitaleAdres = digitaleAdresRequest.asOpenKlant2DigitaleAdresUpdate(),
                )

        return digitaleAdres?.let { DigitaleAdresResponse.fromOpenKlant2DigitaleAdres(digitaleAdres, verificatieModuleConfiguration, verificatieService) }
    }

    @MutationMapping
    suspend fun deleteUserDigitaleAdres(
        authentication: CommonGroundAuthentication,
        @Argument digitaleAdresId: UUID,
    ): Boolean? {
        openklant2Service
            .deleteDigitaleAdresById(
                authentication = authentication,
                digitaleAdresId = digitaleAdresId,
            )
        return null
    }

    suspend fun doVerificatie(
        digitaleAdresRequest: DigitaleAdresRequest,
    ): DigitaleAdresResponse? =
        if (digitaleAdresRequest.verificatieCode != null) {
            // there is a verificatie code. first we need to verify the code
            val verifyResponse =
                verificatieService!!.verify(
                    VerificatieVerifyInput(
                        uuid = digitaleAdresRequest.uuid,
                        waarde = digitaleAdresRequest.waarde,
                        type = VerificatieType.valueOf(digitaleAdresRequest.type.toString()),
                        code = digitaleAdresRequest.verificatieCode,
                    ),
                )

            if (verifyResponse.errorMessage != null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, verifyResponse.errorMessage)
            }

            if (!verifyResponse.verified) {
                DigitaleAdresResponse(
                    waarde = digitaleAdresRequest.waarde,
                    type = digitaleAdresRequest.type,
                    omschrijving = digitaleAdresRequest.omschrijving,
                    referentie = digitaleAdresRequest.omschrijving,
                    uuid = digitaleAdresRequest.uuid,
                    verificatieCodeVerified = false,
                )
            } else {
                null
            }
        } else {
            // create a verificatie request
            val createResponse =
                verificatieService!!.createVerificatie(
                    VerificatieCreateInput(
                        uuid = digitaleAdresRequest.uuid,
                        waarde = digitaleAdresRequest.waarde,
                        type = VerificatieType.valueOf(digitaleAdresRequest.type.toString()),
                    ),
                )

            if (createResponse.errorMessage != null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, createResponse.errorMessage)
            }

            if (!createResponse.success) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Create verificatie was not successful")
            }

            DigitaleAdresResponse(
                waarde = digitaleAdresRequest.waarde,
                type = digitaleAdresRequest.type,
                omschrijving = digitaleAdresRequest.omschrijving,
                referentie = digitaleAdresRequest.omschrijving,
                verificatieNeeded = true,
                uuid = digitaleAdresRequest.uuid,
            )
        }
}