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
package nl.nlportal.verificatie.service

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.ZonedDateTime
import nl.nlportal.verificatie.autoconfigure.VerificatieModuleConfiguration.VerificatieConfigurationProperties
import nl.nlportal.verificatie.client.VerificatieClient
import nl.nlportal.verificatie.client.domain.VerificatieCreateApiRequest
import nl.nlportal.verificatie.client.domain.VerificatieVerifyApiRequest
import nl.nlportal.verificatie.graphql.domain.VerificatieType
import nl.nlportal.verificatie.graphql.domain.VerificatieCreateInput
import nl.nlportal.verificatie.graphql.domain.VerificatieCreateResponse
import nl.nlportal.verificatie.graphql.domain.VerificatieVerifyInput
import nl.nlportal.verificatie.graphql.domain.VerificatieVerifyResponse

class VerificatieService(
    private val verificatieConfigurationProperties: VerificatieConfigurationProperties,
    private val verificatieClient: VerificatieClient,
) {
    suspend fun createVerificatie(
        verificatieCreateInput: VerificatieCreateInput,
    ): VerificatieCreateResponse {
        try {
            val request =
                VerificatieCreateApiRequest(
                    phoneNumber =
                        when (verificatieCreateInput.type) {
                            VerificatieType.TELEFOONNUMMER -> {
                                verificatieCreateInput.waarde
                            }

                            else -> {
                                null
                            }
                        },
                    email =
                        when (verificatieCreateInput.type) {
                            VerificatieType.EMAIL -> {
                                verificatieCreateInput.waarde
                            }

                            else -> {
                                null
                            }
                        },
                    reference = verificatieCreateInput.uuid?.toString(),
                    apiKey = verificatieConfigurationProperties.apiKey,
                    templateId =
                        when (verificatieCreateInput.type) {
                            VerificatieType.TELEFOONNUMMER -> {
                                verificatieConfigurationProperties.templateIdPhoneNumber
                            }

                            else -> {
                                verificatieConfigurationProperties.templateIdEmail
                            }
                        },
                )

            val response = verificatieClient.create(request)
            return VerificatieCreateResponse(
                uuid = verificatieCreateInput.uuid,
                success = response.success,
            )
        } catch (e: Exception) {
            logger.error(e) { "Issue creating verificatie request failed: ${e.message}" }
            return VerificatieCreateResponse(
                uuid = verificatieCreateInput.uuid,
                success = false,
                errorMessage = "Issue createing verificatie request failed: ${e.message}",
            )
        }
    }

    suspend fun verify(
        verificatieVerifyInput: VerificatieVerifyInput,
    ): VerificatieVerifyResponse {
        try {
            val request =
                VerificatieVerifyApiRequest(
                    phoneNumber =
                        when (verificatieVerifyInput.type) {
                            VerificatieType.TELEFOONNUMMER -> {
                                verificatieVerifyInput.waarde
                            }

                            else -> {
                                null
                            }
                        },
                    email =
                        when (verificatieVerifyInput.type) {
                            VerificatieType.EMAIL -> {
                                verificatieVerifyInput.waarde
                            }

                            else -> {
                                null
                            }
                        },
                    reference = verificatieVerifyInput.uuid?.toString(),
                    code = verificatieVerifyInput.code,
                )

            val response = verificatieClient.verify(request)
            return VerificatieVerifyResponse(
                uuid = verificatieVerifyInput.uuid,
                verified = response.verified,
                verifiedOp = response.verifiedOp,
            )
        } catch (e: Exception) {
            logger.error(e) { "Issue verify verificatie request failed: ${e.message}" }
            return VerificatieVerifyResponse(
                uuid = verificatieVerifyInput.uuid,
                verified = false,
                verifiedOp = ZonedDateTime.now(),
                errorMessage = "Issue verify verificatie request failed: ${e.message}",
            )
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}