package nl.nlportal.klant.contactmomenten.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore

data class ContactMoment(
    @GraphQLIgnore
    val url: String,
    val vorigContactmoment: String?,
    val volgendContactmoment: String?,
    val bronorganisatie: String?,
    val registratiedatum: String,
    val kanaal: String,
    val voorkeurskanaal: String?,
    val voorkeurstaal: String?,
    val tekst: String,
    val initiatiefnemer: String?,
    val medewerker: String?
)