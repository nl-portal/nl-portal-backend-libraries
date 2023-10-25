package nl.nlportal.zgw.taak.domain

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore

data class TaakFormulier(
    @Deprecated("Type will be removed when underlying systems has been changed. Currently available for backwards compatibility")
    @GraphQLIgnore
    val type: String?,
    @GraphQLIgnore
    val formuliertype: String?,
    val value: String,
) {
    @GraphQLDescription(value = "Will return only 'portalid', 'objecturl', 'externalurl'")
    fun formuliertype(): String {
        val typeValue = (formuliertype ?: type)!!
        return convertFormulierType(typeValue)
    }

    @Deprecated("To support old formulier types")
    fun convertFormulierType(formuliertype: String): String {
        return when (formuliertype) {
            "id" -> "portalid"
            "url" -> "objecturl"
            else -> formuliertype
        }
    }
}