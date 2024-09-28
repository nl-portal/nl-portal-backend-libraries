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

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.domain.CreatePartij.ContactpersoonIdentificatie
import nl.nlportal.openklant.domain.CreatePartij.OrganisatieIdentificatie
import nl.nlportal.openklant.domain.CreatePartij.PartijIdentificatie
import nl.nlportal.openklant.domain.CreatePartij.PersoonsIdentificatie
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

@GraphQLDescription(value = "A Type that represents a Klantinteracties API Partij object")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Partij(
    val betrokkenen: List<OpenKlant2ForeignKey> = emptyList(),
    val bezoekadres: OpenKlant2Adres? = null,
    val categorieRelaties: List<CategorieRelatieForeignKey>,
    val correspondentieadres: OpenKlant2Adres? = null,
    val digitaleAdressen: List<OpenKlant2ForeignKey>? = null,
    val indicatieActief: Boolean,
    val indicatieGeheimhouding: Boolean? = null,
    val interneNotitie: String? = null,
    val nummer: String? = null,
    val partijIdentificatoren: List<OpenKlant2ForeignKey>,
    val rekeningnummers: List<OpenKlant2ForeignKey>? = null,
    val soortPartij: SoortPartij,
    val url: String,
    val uuid: UUID,
    val vertegenwoordigden: List<OpenKlant2ForeignKey>,
    val voorkeursDigitaalAdres: OpenKlant2ForeignKey? = null,
    val voorkeursRekeningnummer: OpenKlant2ForeignKey? = null,
    val voorkeurstaal: String?,
    @JsonTypeInfo(include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "soortPartij", use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes(
        Type(PersoonsIdentificatie::class, name = "persoon"),
        Type(OrganisatieIdentificatie::class, name = "organisatie"),
        Type(ContactpersoonIdentificatie::class, name = "contactpersoon"),
    )
    val partijIdentificatie: PartijIdentificatie,
    @JsonProperty("_expand")
    val expand: PartijExpand? = null,
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
data class CreatePartij(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val nummer: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val interneNotitie: String? = null,
    val digitaleAdressen: List<OpenKlant2ForeignKey>? = null,
    val voorkeursDigitaalAdres: OpenKlant2ForeignKey? = null,
    val rekeningnummers: List<OpenKlant2ForeignKey>? = null,
    val voorkeursRekeningnummer: OpenKlant2ForeignKey? = null,
    val soortPartij: SoortPartij,
    val indicatieGeheimhouding: Boolean,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val voorkeurstaal: String? = null,
    val indicatieActief: Boolean,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val bezoekadres: OpenKlant2Adres? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val correspondentieadres: OpenKlant2Adres? = null,
    val partijIdentificatoren: List<OpenKlant2ForeignKey>,
    @JsonTypeInfo(include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "soortPartij", use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes(
        Type(PersoonsIdentificatie::class, name = "persoon"),
        Type(OrganisatieIdentificatie::class, name = "organisatie"),
        Type(ContactpersoonIdentificatie::class, name = "contactpersoon"),
    )
    val partijIdentificatie: PartijIdentificatie,
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    open class PersoonsIdentificatie(
        val contactnaam: Contactnaam? = null,
        val volledigeNaam: String? = null,
    ) : PartijIdentificatie

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ContactpersoonIdentificatie(
        val uuid: UUID? = null,
        val werkteVoorPartij: OpenKlant2ForeignKey? = null,
        val contactnaam: Contactnaam? = null,
        val volledigeNaam: String? = null,
    ) : PartijIdentificatie

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class OrganisatieIdentificatie(
        val naam: String? = null,
    ) : PartijIdentificatie

    interface PartijIdentificatie

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Contactnaam(
        val voorletters: String? = null,
        val voornaam: String? = null,
        val voorvoegselAchternaam: String? = null,
        val achternaam: String? = null,
    )
}

class PartijExpand(
    val betrokkenen: List<Betrokkene>? = null,
    val hadKlantcontact: List<HadKlantcontact>? = null,
    val categorieRelaties: List<CategorieRelatie>? = null,
    val digitaleAdressen: List<DigitaleAdres>? = null,
)

data class CategorieRelatieForeignKey(
    val beginDatum: LocalDate? = null,
    val categorieNaam: String,
    val eindDatum: LocalDate? = null,
    val url: String,
    val uuid: String,
)

enum class SoortPartij(
    @JsonValue val value: String,
) {
    PERSOON("persoon"),
    ORGANISATIE("organisatie"),
    CONTACTPERSOON("contactpersoon"),
    ;

    override fun toString(): String {
        return this.value
    }
}

fun CommonGroundAuthentication.asSoortPartij(): String {
    return when (this) {
        is BurgerAuthentication -> "persoon"
        is BedrijfAuthentication -> "organisatie"
        else -> "contactpersoon"
    }
}