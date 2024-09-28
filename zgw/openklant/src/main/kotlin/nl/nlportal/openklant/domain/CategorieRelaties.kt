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

import java.time.LocalDate

data class CategorieRelatie(
    val beginDatum: LocalDate? = null,
    val categorie: Categorie? = null,
    val eindDatum: LocalDate? = null,
    val partij: OpenKlant2ForeignKey? = null,
    val url: String,
    val uuid: String,
)

data class CreateCategorieRelatie(
    val beginDatum: LocalDate? = null,
    val categorie: Categorie? = null,
    val eindDatum: LocalDate? = null,
    val partij: OpenKlant2ForeignKey? = null,
)

data class Categorie(
    val naam: String,
    val url: String,
    val uuid: String,
)