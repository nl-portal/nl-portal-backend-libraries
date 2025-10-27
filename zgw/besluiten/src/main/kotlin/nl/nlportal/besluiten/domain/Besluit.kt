package nl.nlportal.besluiten.domain

import java.time.LocalDate

data class Besluit(
    val url: String,
    val identificatie: String,
    val verantwoordelijkeOrganisatie: String,
    val besluittype: String,
    val zaak: String,
    val datum: LocalDate,
    val toelichting: String?,
    val bestuursorgaan: String?,
    val ingangsdatum: LocalDate,
    val vervaldatum: LocalDate?,
    val vervalreden: String,
    val vervalredenWeergave: String,
    val publicatiedatum: LocalDate?,
    val verzenddatum: LocalDate?,
    val uiterlijkeReactiedatum: LocalDate?,
)
