package nl.nlportal.zgw.taak.domain

import java.util.UUID

data class TaakKoppeling(
    val registratie: TaakKoppelingRegistratie,
    val uuid: UUID,
)