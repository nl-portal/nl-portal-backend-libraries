/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.domain

data class HadKlantcontact(
    val gingOverOnderwerpobjecten: List<OpenKlant2ForeignKey>,
    val hadBetrokkenActoren: List<HadBetrokkenActoren>,
    val hadBetrokkenen: List<OpenKlant2ForeignKey>,
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
)

data class HadBetrokkenActoren(
    val actoridentificator: OpenKlant2Identificator,
    val indicatieActief: Boolean,
    val naam: String,
    val soortActor: String,
    val url: String,
    val uuid: String,
)