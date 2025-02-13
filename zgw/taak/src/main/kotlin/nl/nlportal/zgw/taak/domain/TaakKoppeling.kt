package nl.nlportal.zgw.taak.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import nl.nlportal.core.util.CoreUtils
import java.util.UUID

data class TaakKoppeling(
    val registratie: String,
    val uuid: UUID? = null,
    @GraphQLIgnore
    val value: String? = null,
) {
    fun value(): String? {
        if (uuid != null) {
            return uuid.toString()
        }
        return value
    }

    companion object {
        fun migrate(zaak: String?): TaakKoppeling {
            return if (zaak != null) {
                TaakKoppeling(
                    registratie = TaakKoppelingRegistratie.ZAAK.value,
                    uuid = CoreUtils.extractId(zaak),
                    value = CoreUtils.extractId(zaak).toString(),
                )
            } else {
                TaakKoppeling(
                    registratie = TaakKoppelingRegistratie.ZAAK.value,
                )
            }
        }
    }
}