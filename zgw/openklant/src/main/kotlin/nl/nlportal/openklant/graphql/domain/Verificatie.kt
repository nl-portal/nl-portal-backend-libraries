package nl.nlportal.openklant.graphql.domain

import java.util.UUID

data class VerificatieCreateInput(
    val uuid: UUID,
    val waarde: String,
    val type: DigitaleAdresType,
)

data class VerificatieVerifyInput(
    val uuid: UUID,
    val waarde: String,
    val type: DigitaleAdresType,
    val code: String,
)
