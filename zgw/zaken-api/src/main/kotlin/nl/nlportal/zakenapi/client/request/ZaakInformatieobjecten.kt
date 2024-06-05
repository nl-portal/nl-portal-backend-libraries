package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.domain.ZaakDocument
import java.util.UUID

interface ZaakInformatieobjecten {
    fun search(): SearchZaakInformatieobjecten

    fun get(id: UUID): GetZaakInformatieobject
}

interface SearchZaakInformatieobjecten : Retrieve<List<ZaakDocument>> {
    fun forZaak(zaakUri: String): SearchZaakInformatieobjecten

    fun forZaak(id: UUID): SearchZaakInformatieobjecten

    fun ofInformatieobject(informatieobjectUri: String): SearchZaakInformatieobjecten
}

interface GetZaakInformatieobject : Retrieve<ZaakDocument>