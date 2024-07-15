/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
package nl.nlportal.zakenapi

import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.DocumentStatus.IN_BEWERKING
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.OPENBAAR
import nl.nlportal.zakenapi.domain.ZaakDocument
import nl.nlportal.zakenapi.domain.ZaakRol
import okhttp3.mockwebserver.MockResponse
import java.util.UUID

object TestHelper {
    fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(readFileAsString(fileName))
    }

    private fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName)!!.readText(Charsets.UTF_8)

    val testDocument =
        Document(
            url = "http=//example.com",
            identificatie = "string",
            creatiedatum = "2019-08-24",
            titel = "Passport",
            vertrouwelijkheidaanduiding = OPENBAAR,
            status = IN_BEWERKING,
            formaat = "string",
            bestandsnaam = "passport.jpg",
            bestandsomvang = 0,
            documentapi = "test-documenten-api",
        )

    val testZaakDocument =
        ZaakDocument(
            uuid = "6c4138a3-48c3-4308-a61e-9e89f6eef7a3",
            informatieobject = "https://example.com",
            zaak = "https://example.com",
        )

    val testZaakRol =
        ZaakRol(
            uuid = UUID.fromString("ed39597f-e326-4dd3-bca1-f15a33b7fcb6"),
            zaak = "https://example.com",
        )
}