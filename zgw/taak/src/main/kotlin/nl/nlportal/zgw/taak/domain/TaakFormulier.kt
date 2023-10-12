package nl.nlportal.zgw.taak.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore

data class TaakFormulier(
    @Deprecated("Type will be removed when underlying systems has been changed. Currently available for backwards compatibility")
    @GraphQLIgnore
    val type: String?,
    @GraphQLIgnore
    val formuliertype: String?,
    val value: String
) {
    fun formuliertype(): String {
        return (formuliertype ?: type)!!
    }
}