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
package nl.nlportal.openklant

object TestHelper {
    val emptyPage =
        """
        {
            "count": 0,
            "next": null,
            "previous": null,
            "results": []
        }
        """.trimIndent()

    object Partijen {
        val createPartijRequest =
            """
                            {
                "digitaleAdressen": null,
                "voorkeursDigitaalAdres": null,
                "vertegenwoordigden": null,
                "rekeningnummers": null,
                "voorkeursRekeningnummer": null,
                "partijIdentificatoren": null,
                "soortPartij": "persoon",
                "indicatieGeheimhouding": true,
                "indicatieActief": true,
                "partijIdentificatie": {
                    "contactnaam": {
                        "voorletters": "A.",
                        "voornaam": "Anna",
                        "voorvoegselAchternaam": "",
                        "achternaam": "Vissart"
                    },
                    "volledigeNaam": "Anna Vissart"
                }
            }
            """.trimIndent()
        val persoonPartijResponse =
            """
                                    {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "uuid": "9f8e8009-2d1c-40ae-931e-c08861ba7207",
                        "url": "http://localhost:8007/klantinteracties/api/v1/partijen/9f8e8009-2d1c-40ae-931e-c08861ba7207",
                        "nummer": "0000000001",
                        "interneNotitie": "",
                        "betrokkenen": [
                            {
                                "uuid": "d5de314b-58a4-4d02-9d66-4e68e8927a90",
                                "url": "http://localhost:8007/klantinteracties/api/v1/betrokkenen/d5de314b-58a4-4d02-9d66-4e68e8927a90"
                            }
                        ],
                        "categorieRelaties": [],
                        "digitaleAdressen": [
                            {
                                "uuid": "1300d2ab-13cb-4c12-9818-3b76e2a5d993",
                                "url": "http://localhost:8007/klantinteracties/api/v1/digitaleadressen/1300d2ab-13cb-4c12-9818-3b76e2a5d993"
                            },
                            {
                                "uuid": "2596d218-05fa-4d8f-ab29-8a5c3029dcf2",
                                "url": "http://localhost:8007/klantinteracties/api/v1/digitaleadressen/2596d218-05fa-4d8f-ab29-8a5c3029dcf2"
                            }
                        ],
                        "voorkeursDigitaalAdres": {
                            "uuid": "1300d2ab-13cb-4c12-9818-3b76e2a5d993",
                            "url": "http://localhost:8007/klantinteracties/api/v1/digitaleadressen/1300d2ab-13cb-4c12-9818-3b76e2a5d993"
                        },
                        "vertegenwoordigden": [],
                        "rekeningnummers": [],
                        "voorkeursRekeningnummer": null,
                        "partijIdentificatoren": [
                            {
                                "uuid": "dd328a2e-7b8e-4809-be28-f9f894c11c34",
                                "url": "http://localhost:8007/klantinteracties/api/v1/partij-identificatoren/dd328a2e-7b8e-4809-be28-f9f894c11c34"
                            }
                        ],
                        "soortPartij": "persoon",
                        "indicatieGeheimhouding": true,
                        "voorkeurstaal": "nld",
                        "indicatieActief": true,
                        "bezoekadres": {
                            "nummeraanduidingId": "",
                            "adresregel1": "",
                            "adresregel2": "",
                            "adresregel3": "",
                            "land": ""
                        },
                        "correspondentieadres": {
                            "nummeraanduidingId": "",
                            "adresregel1": "",
                            "adresregel2": "",
                            "adresregel3": "",
                            "land": ""
                        },
                        "partijIdentificatie": {
                            "contactnaam": {
                                "voorletters": "L.",
                                "voornaam": "Lucas",
                                "voorvoegselAchternaam": "",
                                "achternaam": "Boom"
                            },
                            "volledigeNaam": "Lucas Boom"
                        },
                        "_expand": {
                            "betrokkenen": [
                                {
                                    "uuid": "d5de314b-58a4-4d02-9d66-4e68e8927a90",
                                    "url": "http://localhost:8007/klantinteracties/api/v1/betrokkenen/d5de314b-58a4-4d02-9d66-4e68e8927a90",
                                    "wasPartij": {
                                        "uuid": "9f8e8009-2d1c-40ae-931e-c08861ba7207",
                                        "url": "http://localhost:8007/klantinteracties/api/v1/partijen/9f8e8009-2d1c-40ae-931e-c08861ba7207"
                                    },
                                    "hadKlantcontact": {
                                        "uuid": "33549ba5-95f0-44d2-9c63-776ec126bc55",
                                        "url": "http://localhost:8007/klantinteracties/api/v1/klantcontacten/33549ba5-95f0-44d2-9c63-776ec126bc55"
                                    },
                                    "digitaleAdressen": [
                                        {
                                            "uuid": "1300d2ab-13cb-4c12-9818-3b76e2a5d993",
                                            "url": "http://localhost:8007/klantinteracties/api/v1/digitaleadressen/1300d2ab-13cb-4c12-9818-3b76e2a5d993"
                                        },
                                        {
                                            "uuid": "2596d218-05fa-4d8f-ab29-8a5c3029dcf2",
                                            "url": "http://localhost:8007/klantinteracties/api/v1/digitaleadressen/2596d218-05fa-4d8f-ab29-8a5c3029dcf2"
                                        }
                                    ],
                                    "bezoekadres": {
                                        "nummeraanduidingId": "",
                                        "adresregel1": "",
                                        "adresregel2": "",
                                        "adresregel3": "",
                                        "land": ""
                                    },
                                    "correspondentieadres": {
                                        "nummeraanduidingId": "",
                                        "adresregel1": "",
                                        "adresregel2": "",
                                        "adresregel3": "",
                                        "land": ""
                                    },
                                    "contactnaam": {
                                        "voorletters": "L.",
                                        "voornaam": "Lucas",
                                        "voorvoegselAchternaam": "",
                                        "achternaam": "Boom"
                                    },
                                    "volledigeNaam": "Lucas Boom",
                                    "rol": "klant",
                                    "organisatienaam": "",
                                    "initiator": true,
                                    "_expand": {
                                        "hadKlantcontact": {
                                            "uuid": "33549ba5-95f0-44d2-9c63-776ec126bc55",
                                            "url": "http://localhost:8007/klantinteracties/api/v1/klantcontacten/33549ba5-95f0-44d2-9c63-776ec126bc55",
                                            "gingOverOnderwerpobjecten": [
                                                {
                                                    "uuid": "896821a7-0e35-45a8-84d1-0f4f033ee7c5",
                                                    "url": "http://localhost:8007/klantinteracties/api/v1/onderwerpobjecten/896821a7-0e35-45a8-84d1-0f4f033ee7c5"
                                                }
                                            ],
                                            "hadBetrokkenActoren": [
                                                {
                                                    "uuid": "02d4cea3-ebf4-4826-956e-7a5ca1be85ca",
                                                    "url": "http://localhost:8007/klantinteracties/api/v1/actoren/02d4cea3-ebf4-4826-956e-7a5ca1be85ca",
                                                    "naam": "Jasmijn",
                                                    "soortActor": "medewerker",
                                                    "indicatieActief": true,
                                                    "actoridentificator": {
                                                        "objectId": "",
                                                        "codeObjecttype": "",
                                                        "codeRegister": "",
                                                        "codeSoortObjectId": ""
                                                    },
                                                    "actorIdentificatie": {
                                                        "functie": "receptioniste",
                                                        "emailadres": "jasmijn@email.com",
                                                        "telefoonnummer": "693624816"
                                                    }
                                                }
                                            ],
                                            "omvatteBijlagen": [],
                                            "hadBetrokkenen": [
                                                {
                                                    "uuid": "d5de314b-58a4-4d02-9d66-4e68e8927a90",
                                                    "url": "http://localhost:8007/klantinteracties/api/v1/betrokkenen/d5de314b-58a4-4d02-9d66-4e68e8927a90"
                                                }
                                            ],
                                            "leiddeTotInterneTaken": [
                                                {
                                                    "uuid": "58d26043-0cdc-4a46-9110-6acca6e200f2",
                                                    "url": "http://localhost:8007/klantinteracties/api/v1/internetaken/58d26043-0cdc-4a46-9110-6acca6e200f2"
                                                }
                                            ],
                                            "nummer": "0000000001",
                                            "kanaal": "E-mail",
                                            "onderwerp": "Vraag over vergunningsaanvraag",
                                            "inhoud": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras ut aliquam velit. Vestibulum tempus purus vitae vehicula blandit. Aliquam erat volutpat. Suspendisse potenti. Maecenas ultrices condimentum lorem, sit amet aliquet sem sagittis at. Aenean lorem neque, tincidunt at ultrices ut, condimentum sed dui. Quisque sagittis eros eget sapien tempor lobortis. Aliquam magna nisi, ultrices vitae condimentum quis, ullamcorper luctus nulla. Proin condimentum diam lobortis lacinia accumsan. Etiam gravida neque quis lectus facilisis eleifend. Proin finibus non sapien a feugiat. Vestibulum consequat felis vitae felis aliquam faucibus. Morbi pellentesque quam velit, sed interdum quam cursus sed. Curabitur suscipit nunc eu cursus cursus. Etiam suscipit massa vel mauris tristique, non tristique tortor eleifend.",
                                            "indicatieContactGelukt": true,
                                            "taal": "nld",
                                            "vertrouwelijk": true,
                                            "plaatsgevondenOp": "2024-03-06T11:02:24Z"
                                        }
                                    }
                                }
                            ],
                            "digitaleAdressen": [
                                {
                                    "uuid": "1300d2ab-13cb-4c12-9818-3b76e2a5d993",
                                    "url": "http://localhost:8007/klantinteracties/api/v1/digitaleadressen/1300d2ab-13cb-4c12-9818-3b76e2a5d993",
                                    "verstrektDoorBetrokkene": {
                                        "uuid": "d5de314b-58a4-4d02-9d66-4e68e8927a90",
                                        "url": "http://localhost:8007/klantinteracties/api/v1/betrokkenen/d5de314b-58a4-4d02-9d66-4e68e8927a90"
                                    },
                                    "verstrektDoorPartij": {
                                        "uuid": "9f8e8009-2d1c-40ae-931e-c08861ba7207",
                                        "url": "http://localhost:8007/klantinteracties/api/v1/partijen/9f8e8009-2d1c-40ae-931e-c08861ba7207"
                                    },
                                    "adres": "lucas@boom.nl",
                                    "soortDigitaalAdres": "Email",
                                    "omschrijving": "Persoonlijke email adres"
                                },
                                {
                                    "uuid": "2596d218-05fa-4d8f-ab29-8a5c3029dcf2",
                                    "url": "http://localhost:8007/klantinteracties/api/v1/digitaleadressen/2596d218-05fa-4d8f-ab29-8a5c3029dcf2",
                                    "verstrektDoorBetrokkene": {
                                        "uuid": "d5de314b-58a4-4d02-9d66-4e68e8927a90",
                                        "url": "http://localhost:8007/klantinteracties/api/v1/betrokkenen/d5de314b-58a4-4d02-9d66-4e68e8927a90"
                                    },
                                    "verstrektDoorPartij": {
                                        "uuid": "9f8e8009-2d1c-40ae-931e-c08861ba7207",
                                        "url": "http://localhost:8007/klantinteracties/api/v1/partijen/9f8e8009-2d1c-40ae-931e-c08861ba7207"
                                    },
                                    "adres": "0611111111",
                                    "soortDigitaalAdres": "Telefoon",
                                    "omschrijving": "Telefoonnummer van de klant"
                                }
                            ]
                        }
                    }
                ]
            }
            """.trimIndent()
    }
}