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
package nl.nlportal.openproduct

import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.mockwebserver.MockResponse
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.function.Consumer

object TestHelper {
    val ERRORS_JSON_PATH = "$.errors"
    val EXTENSIONS_JSON_PATH = "$.extensions"
    val logger = KotlinLogging.logger {}

    fun mockResponseFromFile(fileName: String): MockResponse =
        MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(readFileAsString(fileName))

    fun mockResponse(body: String): MockResponse =
        MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(body)

    fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName).readText(Charsets.UTF_8)

    fun WebTestClient.ResponseSpec.verifyOnlyDataExists(basePath: String): WebTestClient.BodyContentSpec =
        this
            .expectBody()
            .consumeWith(Consumer { t -> logger.info { t } })
            .jsonPath(basePath)
            .exists()
            .jsonPath(ERRORS_JSON_PATH)
            .doesNotExist()
            .jsonPath(EXTENSIONS_JSON_PATH)
            .doesNotExist()

    val handleDocumentResponse =
        """
        {
           "url": "http://some.domain.com/enkelvoudiginformatieobjecten/095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
           "identificatie": "string",
           "bronorganisatie": "string",
           "creatiedatum": "2021-10-14",
           "titel": "Een titel",
           "vertrouwelijkheidaanduiding": "openbaar",
           "auteur": "string",
           "status": "definitief",
           "formaat": ".pdf",
           "taal": "str",
           "versie": 0,
           "beginRegistratie": "2021-10-14T12:27:43Z",
           "bestandsnaam": "string",
           "inhoud": "http://example.com",
           "bestandsomvang": 0,
           "link": "http://example.com",
           "beschrijving": "string",
           "ontvangstdatum": "2021-10-14",
           "verzenddatum": "2021-10-14",
           "indicatieGebruiksrecht": true,
           "ondertekening": {
             "soort": "analoog",
             "datum": "2021-10-14"
           },
           "integriteit": {
             "algoritme": "crc_16",
             "waarde": "string",
             "datum": "2021-10-14"
           },
           "informatieobjecttype": "http://example.com",
           "locked": true,
           "bestandsdelen": [
             {
               "url": "http://example.com",
               "volgnummer": 0,
               "omvang": 0,
               "inhoud": "http://example.com",
               "voltooid": true,
               "lock": "string"
             }
           ]
         }
        """.trimIndent()
}