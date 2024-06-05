package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.domain.ZaakObject
import java.util.UUID

interface ZaakObjecten {
    fun search(): SearchZaakObjecten

    fun get(id: UUID): GetZaakOject
}

enum class ObjectType(val value: String) {
    ADRES("adres"),
    BESLUIT("besluit"),
    BUURT("buurt"),
    ENKELVOUDIG_DOCUMENT("enkelvoudig_document"),
    GEMEENTE("gemeente"),
    GEMEENTELIJKE_OPENBARE_RUIMTE("gemeentelijke_openbare_ruimte"),
    HUISHOUDEN("huishouden"),
    INRICHTINGSELEMENT("inrichtingselement"),
    KADASTRALE_ONROERENDE_ZAAK("kadastrale_onroerende_zaak"),
    KUNSTWERKDEEL("kunstwerkdeel"),
    MAATSCHAPPELIJKE_ACTIVITEIT("maatschappelijke_activiteit"),
    MEDEWERKER("medewerker"),
    NATUURLIJK_PERSOON("natuurlijk_persoon"),
    NIET_NATUURLIJK_PERSOON("niet_natuurlijk_persoon"),
    OPENBARE_RUIMTE("openbare_ruimte"),
    ORGANISATORISCHE_EENHEID("organisatorische_eenheid"),
    PAND("pand"),
    SPOORBAANDEEL("spoorbaandeel"),
    STATUS("status"),
    TERREINDEEL("terreindeel"),
    TERREIN_GEBOUWD_OBJECT("terrein_gebouwd_object"),
    VESTIGING("vestiging"),
    WATERDEEL("waterdeel"),
    WEGDEEL("wegdeel"),
    WIJK("wijk"),
    WOONPLAATS("woonplaats"),
    WOZ_DEELOBJECT("woz_deelobject"),
    WOZ_OBJECT("woz_object"),
    WOZ_WAARDE("woz_waarde"),
    ZAKELIJK_RECHT("zakelijk_recht"),
    OVERIGE("overige"),
}

interface SearchZaakObjecten : PagedRetrieve<SearchZaakObjecten, ZaakObject> {
    fun forZaak(zaakUri: String): SearchZaakObjecten

    fun forZaak(id: UUID): SearchZaakObjecten

    fun ofObject(objectUri: String): SearchZaakObjecten

    fun ofObjectType(objectType: ObjectType): SearchZaakObjecten
}

interface GetZaakOject : Retrieve<ZaakObject>