package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.handleStatus
import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.Zaak
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.awaitBody
import java.util.UUID

class ZakenImpl(val zakenApiClient: ZakenApiClient) : Zaken {
    override fun search(): SearchZaken {
        return SearchZakenImpl(zakenApiClient)
    }

    override fun get(id: UUID): GetZaak {
        return GetZaakImpl(zakenApiClient, id)
    }
}

class GetZaakImpl(val zakenApiClient: ZakenApiClient, val id: UUID) : GetZaak {
    override suspend fun retrieve(): Zaak {
        return zakenApiClient.webClient
            .get()
            .uri("/zaken/api/v1/zaken/$id")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}

class SearchZakenImpl(val zakenApiClient: ZakenApiClient) : SearchZaken {
    val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()

    override fun withBsn(bsn: String): SearchZaken {
        queryParams.add("rol__betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", bsn)
        return this
    }

    override fun withKvk(kvk: String): SearchZaken {
        queryParams.add("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie", kvk)
        return this
    }

    override fun withUid(uid: String): SearchZaken {
        queryParams.add("rol__betrokkeneIdentificatie__natuurlijkPersoon__anpIdentificatie", uid)
        return this
    }

    override fun isOpen(open: Boolean): SearchZaken {
        queryParams.add("einddatum__isnull", open.toString())
        return this
    }

    override fun ofZaakType(zaakType: String): SearchZaken {
        queryParams.add("zaaktype", zaakType)
        return this
    }

    override fun ofZaakTypes(zaakTypes: List<String>): SearchZaken {
        throw NotImplementedError("List of zaak types are not supported")
    }

    override fun page(page: Int): SearchZaken {
        queryParams.add("page", page.toString())
        return this
    }

    override suspend fun retrieve(): ResultPage<Zaak> {
        return this.zakenApiClient.webClient.get()
            .uri { it.path("/zaken/api/v1/zaken").queryParams(queryParams).build() }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}