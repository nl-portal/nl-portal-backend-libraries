package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.handleStatus
import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.ZaakRol
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.awaitBody
import java.util.UUID

class ZaakRollenImpl(val zakenApiClient: ZakenApiClient) : ZaakRollen {
    override fun search(): SearchZaakRollen {
        return SearchRollenImpl(zakenApiClient)
    }

    override fun get(id: UUID): GetZaakRol {
        return GetRolImpl(zakenApiClient, id)
    }
}

class GetRolImpl(val zakenApiClient: ZakenApiClient, val id: UUID) : GetZaakRol {
    override suspend fun retrieve(): ZaakRol {
        return this.zakenApiClient.webClient.get()
            .uri("/zaken/api/v1/rollen/$id")
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}

class SearchRollenImpl(val zakenApiClient: ZakenApiClient) : SearchZaakRollen {
    val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()

    override fun withBsn(bsn: String): SearchZaakRollen {
        queryParams.add("betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", bsn)
        return this
    }

    override fun withKvk(kvk: String): SearchZaakRollen {
        queryParams.add("betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie", kvk)
        return this
    }

    override fun withUid(uid: String): SearchZaakRollen {
        queryParams.add("betrokkeneIdentificatie__natuurlijkPersoon__anpIdentificatie", uid)
        return this
    }

    override fun forZaak(zaakUrl: String): SearchZaakRollen {
        queryParams.add("zaak", zaakUrl)
        return this
    }

    override fun forZaak(id: UUID): SearchZaakRollen {
        queryParams.add("zaak", this.zakenApiClient.getZaakUrl(id))
        return this
    }

    override fun page(page: Int): SearchZaakRollen {
        queryParams.add("page", page.toString())
        return this
    }

    override suspend fun retrieve(): ResultPage<ZaakRol> {
        return this.zakenApiClient.webClient.get()
            .uri { it.path("/zaken/api/v1/rollen").queryParams(queryParams).build() }
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}