package nl.nlportal.besluiten.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.LocalDate
import java.util.UUID

data class BesluitAuditTrail(
    val uuid: UUID,
    val bron: String,
    val applicatieId: String?,
    val applicatieWeergave: String?,
    val gebruikersId: String?,
    val gebruikersWeergave: String?,
    val actie: String,
    val actieWeergave: String?,
    val resultaat: Int,
    val hoofdObject: String,
    val resource: String,
    val resourceUrl: String,
    val toelichting: String?,
    val resourceWeergave: String,
    val aanmaakdatum: LocalDate?,
    val wijzigingen: BesluitAuditTrailWijzigingen,
)

data class BesluitAuditTrailWijzigingen(
    val oud: ObjectNode?,
    val nieuw: ObjectNode?,
)