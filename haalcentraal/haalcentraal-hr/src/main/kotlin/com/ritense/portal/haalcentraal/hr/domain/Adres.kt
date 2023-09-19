package com.ritense.portal.haalcentraal.hr.domain

data class Adres(
    val type: String,
    val indAfgeschermd: String,
    val volledigAdres: String,
    val straatnaam: String,
    val postcode: String,
    val postbusnummer: Int,
    val plaats: String,
    val land: String,
    val huisnummer: Int,
)