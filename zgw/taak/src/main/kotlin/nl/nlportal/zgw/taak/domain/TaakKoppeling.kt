package nl.nlportal.zgw.taak.domain

import nl.nlportal.core.util.CoreUtils
import java.util.UUID

data class TaakKoppeling(
    val registratie: TaakKoppelingRegistratie,
    val uuid: UUID?,
) {
    companion object {
        fun migrate(zaak: String?): TaakKoppeling {
            return if (zaak != null) {
                TaakKoppeling(
                    registratie = TaakKoppelingRegistratie.ZAAK,
                    uuid = CoreUtils.extractId(zaak),
                )
            } else {
                TaakKoppeling(
                    registratie = TaakKoppelingRegistratie.ZAAK,
                    uuid = null,
                )
            }
        }
    }
}