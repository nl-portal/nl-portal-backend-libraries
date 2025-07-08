package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.domain.ZaakResultaat
import java.util.*

interface ZaakResultaten {
    fun get(id: UUID): GetZaakResultaat
}

interface GetZaakResultaat : Retrieve<ZaakResultaat>
