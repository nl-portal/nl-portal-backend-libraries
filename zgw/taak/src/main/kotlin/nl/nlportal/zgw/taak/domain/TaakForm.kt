package nl.nlportal.zgw.taak.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.core.util.Mapper

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaakForm(
    val formulier: String,
    @GraphQLIgnore
    val data: Map<String, Any>,
    @GraphQLIgnore
    @JsonProperty("verzonden_data")
    var verzondenData: Map<String, Any>? = null,
) {
    fun data(): ObjectNode {
        return Mapper.get().valueToTree(data)
    }
}