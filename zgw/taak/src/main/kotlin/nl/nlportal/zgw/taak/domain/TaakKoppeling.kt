package nl.nlportal.zgw.taak.domain

import java.util.UUID

data class TaakKoppeling(
    val registratie: String,
    val uuid: UUID?,
)