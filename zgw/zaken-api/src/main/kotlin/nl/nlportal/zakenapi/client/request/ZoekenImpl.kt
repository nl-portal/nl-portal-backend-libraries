package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.handleStatus
import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.Zaak
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.awaitBody
import java.util.UUID

class ZoekenImpl(val zakenApiClient: ZakenApiClient) : Zaken {
    override fun search(): SearchZaken {
        return SearchZoekenImpl(zakenApiClient)
    }

    override fun get(id: UUID): GetZaak {
        throw NotImplementedError()
    }
}

class SearchZoekenImpl(val zakenApiClient: ZakenApiClient) : SearchZaken {
    val bodyValue: MultiValueMap<String, Any> = LinkedMultiValueMap()

    override fun withBsn(bsn: String): SearchZaken {
        bodyValue.add("rol__betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", bsn)
        return this
    }

    override fun withKvk(kvk: String): SearchZaken {
        bodyValue.add("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie", kvk)
        return this
    }

    override fun withUid(uid: String): SearchZaken {
        bodyValue.add("rol__betrokkeneIdentificatie__natuurlijkPersoon__anpIdentificatie", uid)
        return this
    }

    override fun ofZaakType(zaakType: String): SearchZaken {
        bodyValue.add("zaaktype", zaakType)
        return this
    }

    override fun ofZaakTypes(zaakTypeIds: List<String>): SearchZaken {
        bodyValue.add("zaaktype__in", zaakTypeIds.map{zakenApiClient.getZaakTypeUrl(it)})
        return this
    }

    override fun page(page: Int): SearchZaken {
        bodyValue.add("page", page.toString())
        return this
    }

    override suspend fun retrieve(): ResultPage<Zaak> {
        return this.zakenApiClient.webClient
            .post()
            .uri("/zaken/api/v1/zaken/_zoek")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(bodyValue)
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}
