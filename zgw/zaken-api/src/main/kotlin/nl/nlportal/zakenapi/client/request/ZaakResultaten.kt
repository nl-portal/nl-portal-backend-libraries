package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.domain.ZaakResultaat
import java.util.UUID

interface ZaakResultaten {
    fun get(id: UUID): GetZaakResultaat
}

interface GetZaakResultaat : Retrieve<ZaakResultaat>
