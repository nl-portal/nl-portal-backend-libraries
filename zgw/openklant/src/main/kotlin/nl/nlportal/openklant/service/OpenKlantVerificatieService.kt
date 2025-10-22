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
package nl.nlportal.openklant.service

import java.time.LocalDate
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties.OpenKlantVerificatieConfigurationProperties
import nl.nlportal.openklant.client.OpenKlant2VerificatieClient
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdresUpdate
import nl.nlportal.openklant.client.domain.VerificatieCreateRequest
import nl.nlportal.openklant.client.domain.VerificatieCreateResponse
import nl.nlportal.openklant.client.domain.VerificatieVerifyRequest
import nl.nlportal.openklant.client.domain.VerificatieVerifyResponse
import nl.nlportal.openklant.graphql.domain.DigitaleAdresType
import nl.nlportal.openklant.graphql.domain.VerificatieCreateInput
import nl.nlportal.openklant.graphql.domain.VerificatieVerifyInput

class OpenKlantVerificatieService(
    private val verificatieConfigurationProperties: OpenKlantVerificatieConfigurationProperties,
    private val verificatieClient: OpenKlant2VerificatieClient,
    private val openKlant2Service: OpenKlant2Service,
) {
    suspend fun createVerificatie(
        verificatieCreateInput: VerificatieCreateInput,
    ): VerificatieCreateResponse {
        val request =
            VerificatieCreateRequest(
                phoneNumber =
                    when (verificatieCreateInput.type) {
                        DigitaleAdresType.TELEFOONNUMMER -> {
                            verificatieCreateInput.waarde
                        }
                        else -> {
                            null
                        }
                    },
                email =
                    when (verificatieCreateInput.type) {
                        DigitaleAdresType.EMAIL -> {
                            verificatieCreateInput.waarde
                        }
                        else -> {
                            null
                        }
                    },
                reference = verificatieCreateInput.uuid.toString(),
                apiKey = verificatieConfigurationProperties.apiKey,
                templateId =
                    when (verificatieCreateInput.type) {
                        DigitaleAdresType.TELEFOONNUMMER -> {
                            verificatieConfigurationProperties.templateIdPhoneNumber
                        }
                        else -> {
                            verificatieConfigurationProperties.templateIdEmail
                        }
                    },
            )

        return verificatieClient.create(request)
    }

    suspend fun verify(
        authentication: CommonGroundAuthentication,
        verificatieVerifyInput: VerificatieVerifyInput,
    ): VerificatieVerifyResponse {
        val request =
            VerificatieVerifyRequest(
                phoneNumber =
                    when (verificatieVerifyInput.type) {
                        DigitaleAdresType.TELEFOONNUMMER -> {
                            verificatieVerifyInput.waarde
                        }
                        else -> {
                            null
                        }
                    },
                email =
                    when (verificatieVerifyInput.type) {
                        DigitaleAdresType.EMAIL -> {
                            verificatieVerifyInput.waarde
                        }
                        else -> {
                            null
                        }
                    },
                reference = verificatieVerifyInput.uuid.toString(),
                code = verificatieVerifyInput.code,
            )

        val response = verificatieClient.verify(request)
        if (response.verified) {
            // set verificatieDatum on digital adres
            val updateDigitalAdres =
                OpenKlant2DigitaleAdresUpdate(
                    uuid = verificatieVerifyInput.uuid,
                    adres = verificatieVerifyInput.waarde,
                    soortDigitaalAdres = verificatieVerifyInput.type.name.lowercase(),
                    verificatieDatum = LocalDate.now(),
                )

            openKlant2Service.updateDigitaleAdresById(
                authentication = authentication,
                digitaleAdres = updateDigitalAdres,
            )
        }

        return response
    }

    fun verificatieEnabled(): Boolean = verificatieConfigurationProperties.isEnabled()
}