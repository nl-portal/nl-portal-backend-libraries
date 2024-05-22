package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.handleStatus
import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.ZaakDocument
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.awaitBody
import java.util.UUID

class ZakenInformatieobjectenImpl(val zakenApiClient: ZakenApiClient) : ZaakInformatieobjecten {
    override fun search(): SearchZaakInformatieobjecten {
        return SearchZaakInformatieobjectenImpl(zakenApiClient)
    }

    override fun get(id: UUID): GetZaakInformatieobject {
        return GetZaakInformatieobjectImpl(zakenApiClient, id)
    }
}

class GetZaakInformatieobjectImpl(val zakenApiClient: ZakenApiClient, val id: UUID) : GetZaakInformatieobject {
    override suspend fun retrieve(): ZaakDocument {
        return this.zakenApiClient.webClient.get()
            .uri("/zaken/api/v1/zaakinformatieobjecten/$id")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}

class SearchZaakInformatieobjectenImpl(val zakenApiClient: ZakenApiClient) : SearchZaakInformatieobjecten {
    val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()

    override fun forZaak(zaakuri: String): SearchZaakInformatieobjecten {
        queryParams.add("zaak", zaakuri)
        return this
    }

    override fun forZaak(id: UUID): SearchZaakInformatieobjecten {
        queryParams.add("zaak", zakenApiClient.getZaakUrl(id))
        return this
    }

    override fun ofInformatieobject(informatieobjectUri: String): SearchZaakInformatieobjecten {
        queryParams.add("informatieobject", informatieobjectUri)
        return this
    }

    override fun page(page: Int): SearchZaakInformatieobjecten {
        queryParams.add("page", page.toString())
        return this
    }

    override suspend fun retrieve(): ResultPage<ZaakDocument> {
        return this.zakenApiClient.webClient.get()
            .uri { it.path("/zaken/api/v1/zaakinformatieobjecten").queryParams(queryParams).build() }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}