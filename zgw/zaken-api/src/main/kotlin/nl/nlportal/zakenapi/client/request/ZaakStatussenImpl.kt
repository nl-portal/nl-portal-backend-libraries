package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.handleStatus
import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.ZaakStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.awaitBody
import java.util.UUID

class ZaakStatussenImpl(val zakenApiClient: ZakenApiClient) : ZaakStatussen {
    override fun search(): SearchZaakStatussen {
        return SearchZaakStatussenImpl(zakenApiClient)
    }

    override fun get(id: UUID): GetZaakStatus {
        return GetZaakStatussenImpl(zakenApiClient, id)
    }
}

class GetZaakStatussenImpl(val zakenApiClient: ZakenApiClient, val id: UUID) : GetZaakStatus {
    override suspend fun retrieve(): ZaakStatus {
        return this.zakenApiClient.webClient.get()
            .uri("/zaken/api/v1/statussen/$id")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}

class SearchZaakStatussenImpl(val zakenApiClient: ZakenApiClient) : SearchZaakStatussen {
    val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()

    override fun forZaak(zaakUrl: String): SearchZaakStatussen {
        queryParams.add("zaak", zaakUrl)
        return this
    }

    override fun forZaak(zaakId: UUID): SearchZaakStatussen {
        queryParams.add("zaak", zakenApiClient.getZaakUrl(zaakId))
        return this
    }

    override fun forStatustype(statustype: String): SearchZaakStatussen {
        queryParams.add("statustype", statustype)
        return this
    }

    override fun page(page: Int): SearchZaakStatussen {
        queryParams.add("page", page.toString())
        return this
    }

    override suspend fun retrieve(): ResultPage<ZaakStatus> {
        return this.zakenApiClient.webClient.get()
            .uri { it.path("/zaken/api/v1/statussen").queryParams(queryParams).build() }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}