package nl.nlportal.zgw.taak.domain

import nl.nlportal.core.util.CoreUtils

data class TaakKoppeling(
    val registratie: String,
    val value: String? = null,
)
