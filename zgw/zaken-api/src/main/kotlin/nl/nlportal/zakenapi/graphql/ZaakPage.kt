package nl.nlportal.zakenapi.graphql

import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.Zaak

class ZaakPage(
    number: Int,
    size: Int,
    content: List<Zaak>,
    totalElements: Int,
) : Page<Zaak>(number, size, content, totalElements) {
    companion object {
        fun fromResultPage(
            pageNumber: Int,
            resultPage: ResultPage<Zaak>,
        ): ZaakPage {
            val zaken = resultPage.results.map { it }

            return ZaakPage(
                number = pageNumber,
                size = 100,
                content = zaken,
                totalElements = resultPage.count,
            )
        }
    }
}