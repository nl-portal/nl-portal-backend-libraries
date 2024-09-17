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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import java.util.Locale
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Partij(
    val betrokkenen: List<ForeignKey> = emptyList(),
    val bezoekadres: Adres? = null,
    val categorieRelaties: List<CategorieRelatieForeignKey>,
    val correspondentieadres: Adres? = null,
    val digitaleAdressen: List<ForeignKey>? = null,
    val indicatieActief: Boolean,
    val indicatieGeheimhouding: Boolean? = null,
    val interneNotitie: String? = null,
    val nummer: String? = null,
    val partijIdentificatoren: List<ForeignKey>,
    val rekeningnummers: List<Rekeningnummer>? = null,
    val soortPartij: SoortPartij,
    val url: String,
    val uuid: UUID,
    val vertegenwoordigden: List<ForeignKey>,
    val voorkeursDigitaalAdres: ForeignKey? = null,
    val voorkeursRekeningnummer: Rekeningnummer? = null,
    val voorkeurstaal: String?,
) {
    init {
        require(voorkeurstaal == null || voorkeurstaal in Locale.getAvailableLocales().map { it.isO3Language }) {
            "Voorkeurstaal must be a valid Language in the ISO 639-2/B format"
        }
        require(interneNotitie == null || interneNotitie.length <= 1000) {
            "Interne notitie can't be longer than 1000 characters."
        }
        require(nummer == null || nummer.length <= 10) {
            "Nummer can't be longer than 10 characters."
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreatePartij(
    val nummer: String? = null,
    val interneNotitie: String? = null,
    val digitaleAdressen: List<ForeignKey>,
    val voorkeursDigitaalAdres: ForeignKey,
    val rekeningnummers: List<Rekeningnummer>,
    val voorkeursRekeningnummer: Rekeningnummer,
    val soortPartij: SoortPartij,
    val indicatieGeheimhouding: Boolean,
    val voorkeurstaal: String? = null,
    val indicatieActief: Boolean,
    val bezoekadres: Adres? = null,
    val correspondentieadres: Adres? = null,
) {
    init {
        require(voorkeurstaal == null || voorkeurstaal in Locale.getAvailableLocales().map { it.isO3Language }) {
            "Voorkeurstaal must be a valid Language in the ISO 639-2/B format"
        }
        require(interneNotitie == null || interneNotitie.length <= 1000) {
            "Interne notitie can't be longer than 1000 characters."
        }
        require(nummer == null || nummer.length <= 10) {
            "Nummer can't be longer than 10 characters."
        }
    }
}

data class PartijenFilterOptions(
    val page: Int = 1,
    val naam: String? = null,
    val indicatieActief: Boolean? = null,
    val soortPartij: SoortPartij? = null,
)

data class Adres(
    val adresregel1: String? = null,
    val adresregel2: String? = null,
    val adresregel3: String? = null,
    val land: Landcode? = null,
    val nummeraanduidingId: String? = null,
) {
    init {
        require(nummeraanduidingId == null || nummeraanduidingId.length <= 255) {
            "Adresregel1 can't be more than 255 characters long."
        }
        require(adresregel1 == null || adresregel1.length <= 80) {
            "Adresregel1 can't be more than 255 characters long."
        }
        require(adresregel2 == null || adresregel2.length <= 80) {
            "Adresregel1 can't be more than 255 characters long."
        }
        require(adresregel3 == null || adresregel3.length <= 80) {
            "Adresregel1 can't be more than 255 characters long."
        }
    }
}

data class CategorieRelatieForeignKey(
    val beginDatum: String,
    val categorieNaam: String,
    val eindDatum: String,
    val url: String,
    val uuid: String,
)

data class Rekeningnummer(val uuid: String)

data class CreatePartij(
    val name: String,
) {
    init {
        require(name.length in 1..200) { "Partij name has to be between 1 and 200 characters long" }
    }
}

enum class SoortPartij(
    @JsonValue private val value: String,
) {
    PERSOON("persoon"),
    ORGANISATIE("organisatie"),
    CONTACTPERSOON("contactpersoon"),
    ;

    override fun toString(): String {
        return this.value
    }
}