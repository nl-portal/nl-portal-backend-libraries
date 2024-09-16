package nl.nlportal.zgw.taak.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class TaakSoort(
    @JsonValue val value: String,
) {
    URL("url"),
    PORTAALFORMULIER("portaalformulier"),
    OGONEBETALING("ogonebetaling"),
}