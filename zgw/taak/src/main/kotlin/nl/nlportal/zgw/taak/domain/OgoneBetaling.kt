package nl.nlportal.zgw.taak.domain

data class OgoneBetaling(
    val bedrag: Double,
    val betaalkenmerk: String,
    val pspid: String,
)