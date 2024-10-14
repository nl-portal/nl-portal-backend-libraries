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

import nl.nlportal.openklant.client.domain.HadBetrokkenActoren
import nl.nlportal.openklant.client.domain.HadKlantcontact
import nl.nlportal.openklant.client.domain.OpenKlant2ForeignKey
import nl.nlportal.openklant.client.domain.OpenKlant2Identificator

data class KlantContactResponse(
    val gingOverOnderwerpobjecten: List<OpenKlant2ForeignKey>,
    val betrokkenActoren: List<BetrokkenActoren>,
    val betrokkenen: List<OpenKlant2ForeignKey>,
    val indicatieContactGelukt: Boolean,
    val inhoud: String,
    val kanaal: String,
    val leiddeTotInterneTaken: List<OpenKlant2ForeignKey>,
    val nummer: String,
    val omvatteBijlagen: List<OpenKlant2ForeignKey>,
    val onderwerp: String,
    val plaatsgevondenOp: String,
    val taal: String,
    val url: String,
    val uuid: String,
    val vertrouwelijk: Boolean,
) {
    companion object {
        fun fromHadKlantContact(hadKlantcontact: HadKlantcontact): KlantContactResponse =
            KlantContactResponse(
                gingOverOnderwerpobjecten = hadKlantcontact.gingOverOnderwerpobjecten,
                betrokkenActoren = hadKlantcontact.hadBetrokkenActoren.map { BetrokkenActoren.fromHadBetrokkenActoren(it) },
                betrokkenen = hadKlantcontact.hadBetrokkenen,
                indicatieContactGelukt = hadKlantcontact.indicatieContactGelukt,
                inhoud = hadKlantcontact.inhoud,
                kanaal = hadKlantcontact.kanaal,
                leiddeTotInterneTaken = hadKlantcontact.leiddeTotInterneTaken,
                nummer = hadKlantcontact.nummer,
                omvatteBijlagen = hadKlantcontact.omvatteBijlagen,
                onderwerp = hadKlantcontact.onderwerp,
                plaatsgevondenOp = hadKlantcontact.plaatsgevondenOp,
                taal = hadKlantcontact.taal,
                url = hadKlantcontact.url,
                uuid = hadKlantcontact.uuid,
                vertrouwelijk = hadKlantcontact.vertrouwelijk,
            )
    }
}

data class BetrokkenActoren(
    val actoridentificator: OpenKlant2Identificator,
    val indicatieActief: Boolean,
    val naam: String,
    val soortActor: String,
    val url: String,
    val uuid: String,
) {
    companion object {
        fun fromHadBetrokkenActoren(hadBetrokkenActoren: HadBetrokkenActoren): BetrokkenActoren =
            BetrokkenActoren(
                actoridentificator = hadBetrokkenActoren.actoridentificator,
                indicatieActief = hadBetrokkenActoren.indicatieActief,
                naam = hadBetrokkenActoren.naam,
                soortActor = hadBetrokkenActoren.soortActor,
                url = hadBetrokkenActoren.url,
                uuid = hadBetrokkenActoren.uuid,
            )
    }
}