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

import nl.nlportal.openklant.domain.CreatePartij.Contactnaam

data class Betrokkene(
    val bezoekadres: OpenKlant2Adres? = null,
    val contactnaam: Contactnaam? = null,
    val correspondentieadres: OpenKlant2Adres? = null,
    val digitaleAdressen: List<OpenKlant2ForeignKey>,
    val hadKlantcontact: OpenKlant2ForeignKey,
    val initiator: Boolean,
    val organisatienaam: String,
    val rol: String,
    val url: String,
    val uuid: String,
    val volledigeNaam: String,
    val wasPartij: OpenKlant2ForeignKey? = null,
)

data class CreateBetrokkene(
    val bezoekadres: OpenKlant2Adres? = null,
    val contactnaam: Contactnaam? = null,
    val correspondentieadres: OpenKlant2Adres? = null,
    val digitaleAdressen: List<OpenKlant2ForeignKey>,
    val hadKlantcontact: OpenKlant2ForeignKey,
    val initiator: Boolean,
    val organisatienaam: String,
    val rol: String,
    val volledigeNaam: String,
    val wasPartij: OpenKlant2ForeignKey? = null,
)