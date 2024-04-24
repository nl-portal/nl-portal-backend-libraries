package nl.nlportal.startform.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode

class StartFormObject(
    @JsonProperty("aanvrager") val aanvragerIdentificatie: AanvragerIdentificatie,
    val data: ObjectNode
) {
}