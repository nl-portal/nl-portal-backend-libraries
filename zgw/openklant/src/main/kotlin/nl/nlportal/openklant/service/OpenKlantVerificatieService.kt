package nl.nlportal.openklant.service

import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties.OpenKlantVerificatieConfigurationProperties
import nl.nlportal.openklant.client.OpenKlant2VerificatieClient
import nl.nlportal.openklant.client.domain.VerificatieCreateRequest
import nl.nlportal.openklant.client.domain.VerificatieCreateResponse
import nl.nlportal.openklant.client.domain.VerificatieVerifyRequest
import nl.nlportal.openklant.client.domain.VerificatieVerifyResponse
import nl.nlportal.openklant.graphql.domain.VerificatieCreateInput
import nl.nlportal.openklant.graphql.domain.VerificatieVerifyInput

class OpenKlantVerificatieService(
    private val verificatieConfigurationProperties: OpenKlantVerificatieConfigurationProperties,
    private val verificatieClient: OpenKlant2VerificatieClient,
    private val openKlant2Service: OpenKlant2Service
) {

    suspend fun createVerificatie(
        verificatieCreateInput: VerificatieCreateInput
    ): VerificatieCreateResponse {
        val request = VerificatieCreateRequest(
            phoneNumber = verificatieCreateInput.phoneNumber,
            email = verificatieCreateInput.email,
            reference = verificatieCreateInput.digitalAdresId.toString(),
            apiKey = verificatieConfigurationProperties.apiKey,
            templateId = when (verificatieCreateInput.phoneNumber) {
                null -> {
                    verificatieConfigurationProperties.templateIdEmail
                }
                else -> {
                    verificatieConfigurationProperties.templateIdPhoneNumber
                }
            },
        )

        return verificatieClient.create(request)
    }

    suspend fun verify(
        verificatieVerifyInput: VerificatieVerifyInput
    ): VerificatieVerifyResponse {
        val request = VerificatieVerifyRequest(
            phoneNumber = verificatieVerifyInput.phoneNumber,
            email = verificatieVerifyInput.email,
            reference = verificatieVerifyInput.digitalAdresId.toString(),
            code = verificatieVerifyInput.code,
        )

        val response = verificatieClient.verify(request)
        if(response.verified) {
            //set verificatieDatum on digital adres

        }

        return response
    }
}
