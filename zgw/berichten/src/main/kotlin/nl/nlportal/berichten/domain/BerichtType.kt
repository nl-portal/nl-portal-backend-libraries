package nl.nlportal.berichten.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class BerichtType(
    @JsonValue val value: String,
) {
    TODO("add"),
}