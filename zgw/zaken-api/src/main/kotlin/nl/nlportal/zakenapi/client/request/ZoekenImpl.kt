/*
 * Copyright 2025 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()

    override fun withBsn(bsn: String): SearchZaken {
        bodyValue.add("rol__betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", bsn)
        return this
    }

    override fun withKvk(kvk: String): SearchZaken {
        when {
            zakenApiClient.useNnpKvkQueryIdentificators() -> {
                bodyValue.add("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__kvkNummer", kvk)
            }
            else -> {
                bodyValue.add("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie", kvk)
            }
        }
        return this
    }

    override fun withUid(uid: String): SearchZaken {
        bodyValue.add("rol__betrokkeneIdentificatie__natuurlijkPersoon__anpIdentificatie", uid)
        return this
    }

    override fun isOpen(open: Boolean): SearchZaken {
        bodyValue.add("einddatum__isnull", open)
        return this
    }

    override fun ofZaakType(zaakType: String): SearchZaken {
        bodyValue.add("zaaktype", zaakType)
        return this
    }

    override fun ofZaakTypes(zaakTypeIds: List<UUID>): SearchZaken {
        bodyValue.put(
            "zaaktype__in",
            zaakTypeIds.map {
                zakenApiClient.getZaakTypeUrl(it)
            },
        )
        return this
    }

    override fun notInZaakTypes(zaakTypeIds: List<UUID>): SearchZaken {
        bodyValue.put(
            "zaaktype__not_in",
            zaakTypeIds.map {
                zakenApiClient.getZaakTypeUrl(it)
            },
        )
        return this
    }

    override fun ofIdentificatie(identificatie: String): SearchZaken {
        bodyValue.add("identificatie", identificatie)
        return this
    }

    override fun ofIdenfitifactieContains(identificatie: String): SearchZaken {
        bodyValue.add("identificatie__icontains", identificatie)
        return this
    }

    override fun ofOmschrijving(omschrijving: String): SearchZaken {
        bodyValue.add("omschrijving", omschrijving)
        return this
    }

    override fun withKvkAndVestigingsNummer(
        kvkNummer: String,
        vestigingsNummer: String,
    ): SearchZaken {
        when {
            zakenApiClient.useNnpKvkQueryIdentificators() -> {
                bodyValue.add("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__vestigingsNummer", vestigingsNummer)
                bodyValue.add("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__kvkNummer", kvkNummer)
            }
            else -> {
                bodyValue.add("rol__betrokkeneIdentificatie__vestiging__vestigingsNummer", vestigingsNummer)
                bodyValue.add("rol__betrokkeneIdentificatie__vestiging__kvkNummer", kvkNummer)
            }
        }
        return this
    }

    // needs to be added as querystring parameter
    override fun page(page: Int): SearchZaken {
        queryParams.add("page", page.toString())
        return this
    }

    // needs to be added as querystring parameter
    override fun pageSize(pageSize: Int): SearchZaken {
        queryParams.add("pageSize", pageSize.toString())
        return this
    }

    override fun withRolOmschrijvingGeneriek(): SearchZaken {
        bodyValue.add("rol__omschrijvingGeneriek", "initiator")
        return this
    }

    override suspend fun retrieve(): ResultPage<Zaak> {
        return this.zakenApiClient.webClient
            .post()
            .uri { it.path("/zaken/api/v1/zaken/_zoek").queryParams(queryParams).build() }
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(bodyValue)
            .retrieve()
            .handleStatus()
            .awaitBody()
    }
}
