package nl.nlportal.openklant.graphql.domain

import java.util.UUID

data class VerificatieCreateInput(
    val digitalAdresId: UUID,
    val email: String? = null,
    val phoneNumber: String? = null,
)

data class VerificatieVerifyInput(
    val digitalAdresId: UUID,
    val code: String,
    val email: String? = null,
    val phoneNumber: String? = null,
)
