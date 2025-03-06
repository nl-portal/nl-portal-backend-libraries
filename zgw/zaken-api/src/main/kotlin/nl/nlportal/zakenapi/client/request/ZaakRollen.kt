package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.domain.ZaakRol
import java.util.UUID

interface ZaakRollen {
    fun search(): SearchZaakRollen

    fun get(id: UUID): GetZaakRol
}

interface SearchZaakRollen : PagedRetrieve<SearchZaakRollen, ZaakRol>, AuthenticationFilter<SearchZaakRollen> {
    fun forZaak(zaakUrl: String): SearchZaakRollen

    fun forZaak(zaakId: UUID): SearchZaakRollen
}

interface GetZaakRol : Retrieve<ZaakRol>