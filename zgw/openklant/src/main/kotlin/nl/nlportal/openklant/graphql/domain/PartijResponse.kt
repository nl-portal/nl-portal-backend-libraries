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
import nl.nlportal.openklant.client.domain.HadKlantcontact
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdres
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OrganisatieIdentificatie
import nl.nlportal.openklant.client.domain.PersoonsIdentificatie

data class PartijResponse(
    val indicatieGeheimhouding: Boolean,
    val indicatieActief: Boolean,
    val type: PartijType,
    val persoonsIdentificatie: PersoonsIdentificatie? = null,
    val organisatieIdentificatie: OrganisatieIdentificatie? = null,
    val contactpersoonIdentificatie: ContactpersoonIdentificatie? = null,
    val digitaleAdressen: List<OpenKlant2DigitaleAdres>? = null,
    val klantcontacten: List<HadKlantcontact>? = null,
) {
    companion object {
        fun fromOpenKlant2Partij(openKlant2Partij: OpenKlant2Partij): PartijResponse =
            PartijResponse(
                indicatieGeheimhouding = openKlant2Partij.indicatieGeheimhouding,
                indicatieActief = openKlant2Partij.indicatieActief,
                type = PartijType.valueOf(openKlant2Partij.soortPartij.name),
                persoonsIdentificatie = openKlant2Partij.partijIdentificatie as? PersoonsIdentificatie,
                organisatieIdentificatie = openKlant2Partij.partijIdentificatie as? OrganisatieIdentificatie,
                contactpersoonIdentificatie = openKlant2Partij.partijIdentificatie as? ContactpersoonIdentificatie,
                digitaleAdressen = openKlant2Partij.expand?.digitaleAdressen,
                klantcontacten = openKlant2Partij.expand?.hadKlantcontact,
            )
    }
}