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