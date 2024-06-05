package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.domain.Zaak
import java.util.UUID

interface Zaken {
    fun search(): SearchZaken

    fun get(id: UUID): GetZaak
}

interface SearchZaken : PagedRetrieve<SearchZaken, Zaak>, AuthenticationFilter<SearchZaken> {
    fun ofZaakType(zaakType: String): SearchZaken
}

interface GetZaak : Retrieve<Zaak>