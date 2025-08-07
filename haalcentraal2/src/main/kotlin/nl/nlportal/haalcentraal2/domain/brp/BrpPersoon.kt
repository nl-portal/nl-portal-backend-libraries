/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
package nl.nlportal.haalcentraal2.domain.brp

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.haalcentraal2.service.HaalCentraal2Service
import org.springframework.beans.factory.annotation.Autowired

data class BrpPersoon(
    val burgerservicenummer: String,
    val datumEersteInschrijvingGBA: BrpDatum? = null,
    val geheimhoudingPersoonsgegevens: Boolean? = false,
    val geslacht: BrpCodeOmschrijving? = null,
    val inOnderzoek: BrpInOnderzoek? = null,
    val uitsluitingKiesrecht: BrpUitsluitingKiesrecht? = null,
    val europeesKiesrecht: BrpEuropeesKiesrecht? = null,
    val leeftijd: Int,
    val naam: BrpNaam,
    val nationaliteiten: List<BrpNationaliteit>,
    val geboorte: BrpDatumLandPlaats? = null,
    val overlijden: BrpDatumLandPlaats? = null,
    val verblijfplaats: BrpVerblijfplaats? = null,
    val imigratie: BrpImigratie? = null,
    val gemeenteVanInschrijving: BrpCodeOmschrijving? = null,
    val datumInschrijvingInGemeente: BrpDatum? = null,
    val adressering: BrpAdressering? = null,
    val indicatieCurateleRegister: Boolean? = false,
    val indicatieGezagMinderjarige: BrpCodeOmschrijving? = null,
    val gezag: List<BrpGezag>? = null,
    val verblijfstitel: BrpVerblijfsTitel? = null,
    val kinderen: List<BrpKind>? = null,
    val ouders: List<BrpOuder>? = null,
    val partners: List<BrpPartner>? = null,
    val rni: List<BrpPersoonRni>? = null,
    val verificatie: BrpPersoonVerificatie? = null,
) {
    suspend fun bewonersAantal(
        @GraphQLIgnore
        @Autowired
        haalCentraal2Service: HaalCentraal2Service,
        dfe: DataFetchingEnvironment,
    ): Int? =
        verblijfplaats?.adresseerbaarObjectIdentificatie?.let {
            haalCentraal2Service.getBewonersAantal(
                authentication = dfe.graphQlContext[AUTHENTICATION_KEY],
                it,
            )
        }
}

data class BrpPersoonRni(
    val deelnemer: BrpCodeOmschrijving? = null,
    val omschrijvingVerdrag: String? = null,
    val categorie: String? = null,
)

data class BrpPersoonVerificatie(
    val datum: BrpDatum? = null,
    val omschrijving: String? = null,
)