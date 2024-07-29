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
package nl.nlportal.berichten

import okhttp3.mockwebserver.MockResponse

object TestHelper {
    fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(readFileAsString(fileName))
    }

    private fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName).readText(Charsets.UTF_8)

    val testBerichtenRequest =
        """
        query {
            getBerichten(pageNumber: 1, pageSize: 20) {
                content {
                    berichtTekst
                    berichtType
                    bijlages
                    einddatumHandelingstermijn
                    geopend
                    handelingsperspectief
                    identificatie {
                        type
                        value
                        __typename
                    }
                    onderwerp
                    publicatiedatum
                    referentie
                    __typename
                }
                totalElements
                totalPages
                __typename
            }
        }
        """.trimIndent()

    val validBerichtRequest =
        """
        query {
            getBericht(id: "9e021130-8cbd-4c6f-846a-677448e21ce8") {
                berichtTekst
                berichtType
                bijlages
                einddatumHandelingstermijn
                geopend
                handelingsperspectief
                identificatie {
                    type
                    value
                    __typename
                }
                onderwerp
                publicatiedatum
                referentie
                __typename
            }
        }
        """.trimIndent()

    val invalidBerichtRequest =
        """
        query {
            getBericht(id: "9e021130-8cbd-4c6f-846a-677448e21ce6") {
                berichtTekst
                berichtType
                bijlages
                einddatumHandelingstermijn
                geopend
                handelingsperspectief
                identificatie {
                    type
                    value
                    __typename
                }
                onderwerp
                publicatiedatum
                referentie
                __typename
            }
        }
        """.trimIndent()

    val testBerichtResponse =
        """
        {
            "data": {
                "getBericht": {
                    "berichtTekst": "Er zijn werkzaamheden komende week in uw buurt. U kunt meer over dit lezen op de volgende website: https://example.com",
                    "berichtType": "NOTIFICATIE",
                    "bijlages": [
                        "https://example.com/documenten/api/v1/enkelvoudiginformatieobjecten/1"
                    ],
                    "einddatumHandelingstermijn": "2024-10-31",
                    "geopend": false,
                    "handelingsperspectief": "INFORMATIE_ONTVANGEN",
                    "identificatie": {
                        "type": "bsn",
                        "value": "999990755",
                        "__typename": "BerichtIdentificatie"
                    },
                    "onderwerp": "Bericht over uw buurt.",
                    "publicatiedatum": "2024-07-18",
                    "referentie": "ZAAK-2024-0000000001",
                    "__typename": "Bericht"
                }
            }
        }
        """.trimIndent()

    val testBerichtenResponse =
        """
        {
            "data": {
                "getBerichten": {
                    "content": [
                        {
                            "berichtTekst": "Er zijn werkzaamheden komende week in uw buurt. U kunt meer over dit lezen op de volgende website: https://example.com",
                            "berichtType": "NOTIFICATIE",
                            "bijlages": [
                                "https://example.com/documenten/api/v1/enkelvoudiginformatieobjecten/1"
                            ],
                            "einddatumHandelingstermijn": "2024-10-31",
                            "geopend": false,
                            "handelingsperspectief": "INFORMATIE_ONTVANGEN",
                            "identificatie": {
                                "type": "bsn",
                                "value": "999990755",
                                "__typename": "BerichtIdentificatie"
                            },
                            "onderwerp": "Bericht over uw buurt.",
                            "publicatiedatum": "2024-07-18",
                            "referentie": "ZAAK-2024-0000000001",
                            "__typename": "Bericht"
                        }
                    ],
                    "totalElements": 1,
                    "totalPages": 1,
                    "__typename": "BerichtenPage"
                }
            }
        }
        """.trimIndent()

    val testObjectenResponse =
        """
        {
            "count": 1,
            "next": null,
            "previous": null,
            "results": [
                {
                    "url": "http://localhost:8010/api/v2/objects/9e021130-8cbd-4c6f-846a-677448e21ce8",
                    "uuid": "9e021130-8cbd-4c6f-846a-677448e21ce8",
                    "type": "http://host.docker.internal:8011/api/v1/objecttypes/78731088-430f-49fd-9a4c-80ddd42ded28",
                    "record": {
                        "index": 1,
                        "typeVersion": 1,
                        "data": {
                            "geopend": false,
                            "bijlages": [
                                "https://example.com/documenten/api/v1/enkelvoudiginformatieobjecten/1"
                            ],
                            "onderwerp": "Bericht over uw buurt.",
                            "referentie": "ZAAK-2024-0000000001",
                            "berichtType": "notificatie",
                            "berichtTekst": "Er zijn werkzaamheden komende week in uw buurt. U kunt meer over dit lezen op de volgende website: https://example.com",
                            "identificatie": {
                                "type": "bsn",
                                "value": "999990755"
                            },
                            "publicatiedatum": "2024-07-18",
                            "handelingsperspectief": "informatie ontvangen",
                            "einddatumHandelingstermijn": "2024-10-31"
                        },
                        "geometry": null,
                        "startAt": "2024-07-18",
                        "endAt": null,
                        "registrationAt": "2024-07-18",
                        "correctionFor": null,
                        "correctedBy": null
                    }
                }
            ]
        }
        """.trimIndent()
}