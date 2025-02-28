package nl.nlportal.zgw.taak.domain

import nl.nlportal.core.util.CoreUtils

data class TaakKoppeling(
    val registratie: String,
    val value: String? = null,
) {
    companion object {
        fun migrate(zaak: String?): TaakKoppeling {
            return if (zaak != null) {
                TaakKoppeling(
                    registratie = TaakKoppelingRegistratie.ZAAK.value,
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