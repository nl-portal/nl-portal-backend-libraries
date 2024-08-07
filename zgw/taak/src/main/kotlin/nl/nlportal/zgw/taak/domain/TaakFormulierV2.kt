package nl.nlportal.zgw.taak.domain

data class TaakFormulierV2(
    val soort: String,
    val value: String,
) {
    companion object {
        fun migrate(taakFormulier: TaakFormulier): TaakFormulierV2 {
            val value = taakFormulier.value
            return if (value.startsWith("http")) {
                TaakFormulierV2("url", value)
            } else {
                TaakFormulierV2("id", value)
            }
        }
    }
}