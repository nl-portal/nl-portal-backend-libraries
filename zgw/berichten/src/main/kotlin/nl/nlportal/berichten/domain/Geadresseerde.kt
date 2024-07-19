package nl.nlportal.berichten.domain

data class Geadresseerde(
    val type: GeadresseerdeType,
    val value: String,
)
