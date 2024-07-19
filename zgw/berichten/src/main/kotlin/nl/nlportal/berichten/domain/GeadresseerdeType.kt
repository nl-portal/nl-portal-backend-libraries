package nl.nlportal.berichten.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class GeadresseerdeType(
    @JsonValue val value: String,
) {
    BSN("bsn"),
    KVK("kvk"),
    UID("uid"),
}