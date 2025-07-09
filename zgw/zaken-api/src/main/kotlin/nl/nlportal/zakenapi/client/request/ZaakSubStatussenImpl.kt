package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.handleStatus
import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.ZaakSubStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.awaitBody
import java.util.*

class ZaakSubStatussenImpl(val zakenApiClient: ZakenApiClient) : ZaakSubStatussen {
    override fun search(): SearchZaakSubStatussen {
        return SearchZaakSubStatussenImpl(zakenApiClient)
    }

    override fun get(id: UUID): GetZaakSubStatus {
        return GetZaakSubStatussenImpl(zakenApiClient, id)
    }
}

class GetZaakSubStatussenImpl(val zakenApiClient: ZakenApiClient, val id: UUID) : GetZaakSubStatus {
    override suspend fun retrieve(): ZaakSubStatus {
        return this.zakenApiClient.webClient.get()
            .uri("/zaken/api/v1/substatussen/$id")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}

class SearchZaakSubStatussenImpl(val zakenApiClient: ZakenApiClient) : SearchZaakSubStatussen {
    val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()

    override fun forZaak(zaakUrl: String): SearchZaakSubStatussen {
        queryParams.add("zaak", zaakUrl)
        return this
    }

    override fun forStatus(zaakStatusUrl: String): SearchZaakSubStatussen {
        queryParams.add("status", zaakStatusUrl)
        return this
    }

    override fun page(page: Int): SearchZaakSubStatussen {
        queryParams.add("page", page.toString())
        return this
    }

    override fun pageSize(page: Int): SearchZaakSubStatussen {
        queryParams.add("pageSize", page.toString())
        return this
    }

    override suspend fun retrieve(): ResultPage<ZaakSubStatus> {
        return this.zakenApiClient.webClient.get()
            .uri { it.path("/zaken/api/v1/substatussen").queryParams(queryParams).build() }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}
