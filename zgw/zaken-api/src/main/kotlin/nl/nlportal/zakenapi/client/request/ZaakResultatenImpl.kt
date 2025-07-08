package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.handleStatus
import nl.nlportal.zakenapi.domain.ZaakResultaat
import org.springframework.web.reactive.function.client.awaitBody
import java.util.*

class ZaakResultatenImpl(val zakenApiClient: ZakenApiClient) : ZaakResultaten {
    override fun get(id: UUID): GetZaakResultaat {
        return GetZaakResultatenImpl(zakenApiClient, id)
    }
}

class GetZaakResultatenImpl(val zakenApiClient: ZakenApiClient, val id: UUID) : GetZaakResultaat {
    override suspend fun retrieve(): ZaakResultaat {
        return this.zakenApiClient.webClient.get()
            .uri("/zaken/api/v1/resultaten/$id")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}
