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
    val verificatieService: OpenKlantVerificatieService
): Mutation {

    suspend fun createVerificatie(
        verificatieCreateInput: VerificatieCreateInput
    ): VerificatieCreateResponse {
        return verificatieService.createVerificatie(
            verificatieCreateInput = verificatieCreateInput
        )
    }

    suspend fun verify(
        dfe: DataFetchingEnvironment,
        verificatieVerifyInput: VerificatieVerifyInput
    ): VerificatieVerifyResponse {
        return verificatieService.verify(
            authentication = dfe.graphQlContext.get(AUTHENTICATION_KEY),
            verificatieVerifyInput = verificatieVerifyInput
        )
    }
}
