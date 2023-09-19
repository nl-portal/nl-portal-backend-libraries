package com.ritense.portal.haalcentraal.hr.domain

data class Hoofdvestiging(
    val vestigingsnummer: String,
    val kvkNummer: String,
    val eersteHandelsnaam: String,
    val indHoofdvestiging: String,
    val indCommercieleVestiging: String,
    val totaalWerkzamePersonen: Int,
    val adressen: List<Adres>?
)