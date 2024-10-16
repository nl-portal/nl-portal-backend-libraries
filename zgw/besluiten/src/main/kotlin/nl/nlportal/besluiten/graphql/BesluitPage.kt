package nl.nlportal.besluiten.graphql

import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.graphql.Page

class BesluitPage(
    number: Int,
    size: Int,
    content: List<Besluit>,
    totalElements: Int,
) : Page<Besluit>(number, size, content, totalElements) {
    companion object {
        fun fromList(
            pageNumber: Int?,
            besluiten: List<Besluit>,
        ): BesluitPage {
            return BesluitPage(
                number = pageNumber ?: 1,
                size = 100,
                content = besluiten,
                totalElements = besluiten.size,
            )
        }
    }
}