package nl.nlportal.berichten.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Bericht(
    val berichtType: BerichtType,
    val berichttekst: String,
    val einddatumAfhandelingstermijn: LocalDate,
    val geadresseerde: Geadresseerde,
    val handelingsperspectief: String,
    val isGeopend: Boolean,
    val onderwerp: String,
    val publicatiedatum: LocalDate,
    val referentie: String? = null,
)