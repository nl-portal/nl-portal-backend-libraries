/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.graphql.domain

import nl.nlportal.openklant.client.domain.ContactpersoonIdentificatie
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OrganisatieIdentificatie
import nl.nlportal.openklant.client.domain.PartijIdentificatie
import nl.nlportal.openklant.client.domain.PersoonsIdentificatie
import nl.nlportal.openklant.graphql.domain.PartijType.CONTACTPERSOON
import nl.nlportal.openklant.graphql.domain.PartijType.ORGANISATIE
import nl.nlportal.openklant.graphql.domain.PartijType.PERSOON

data class PartijRequest(
    val indicatieGeheimhouding: Boolean,
    val indicatieActief: Boolean,
    val type: PartijType,
    val persoonsIdentificatie: PersoonsIdentificatie? = null,
    val organisatieIdentificatie: OrganisatieIdentificatie? = null,
    val contactpersoonIdentificatie: ContactpersoonIdentificatie? = null,
) {
    private val identificatie: PartijIdentificatie =
        when (type) {
            PERSOON ->
                requireNotNull(persoonsIdentificatie) {
                    "{persoonIdentification} can not be null when <type> is $type"
                }
            ORGANISATIE ->
                requireNotNull(organisatieIdentificatie) {
                    "{organisatieIdentificatie} can not be null when <type> is $type"
                }
            CONTACTPERSOON ->
                requireNotNull(contactpersoonIdentificatie) {
                    "{contactpersoonIdentificatie} can not be null when <type> is $type"
                }
        }

    fun asOpenKlant2Partij(): OpenKlant2Partij =
        OpenKlant2Partij(
            indicatieGeheimhouding = indicatieGeheimhouding,
            indicatieActief = indicatieActief,
            soortPartij = type.asSoortPartij(),
            partijIdentificatie = identificatie,
        )
}