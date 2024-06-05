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
package nl.nlportal.haalcentraal.brp.domain.persoon

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.haalcentraal.brp.service.HaalCentraalBrpService
import org.springframework.beans.factory.annotation.Autowired

data class Persoon(
    val burgerservicenummer: String? = null,
    val geslachtsaanduiding: String? = null,
    val naam: PersoonNaam? = null,
    val geboorte: PersoonGeboorte? = null,
    val nationaliteiten: List<PersoonNationaliteiten>? = null,
    val verblijfplaats: PersoonVerblijfplaats? = null,
) {
    suspend fun bewonersAantal(
        @GraphQLIgnore
        @Autowired
        haalCentraalBrpService: HaalCentraalBrpService,
        dfe: DataFetchingEnvironment,
    ): Int? {
        return verblijfplaats?.adresseerbaarObjectIdentificatie?.let {
            haalCentraalBrpService.getBewonersAantal(dfe.graphQlContext[AUTHENTICATION_KEY], it)
        }
    }
}