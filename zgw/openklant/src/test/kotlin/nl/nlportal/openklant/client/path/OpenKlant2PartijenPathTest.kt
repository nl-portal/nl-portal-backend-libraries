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
package nl.nlportal.openklant.client.path

import kotlinx.coroutines.test.runTest
import nl.nlportal.openklant.TestHelper
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties
import nl.nlportal.openklant.client.OpenKlant2KlantinteractiesClient
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OpenKlant2PartijenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2PartijenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID
import nl.nlportal.openklant.client.domain.OpenKlant2PartijenFilters.SOORT_PARTIJ
import nl.nlportal.openklant.client.domain.SoortPartij
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times

class OpenKlant2PartijenPathTest {
    private lateinit var openklantModuleConfiguration: OpenKlantModuleConfiguration
    private lateinit var mockServer: MockWebServer
    private lateinit var openKlant2Client: OpenKlant2KlantinteractiesClient

    @BeforeEach
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()

        openklantModuleConfiguration =
            OpenKlantModuleConfiguration(
                enabled = true,
                properties =
                    OpenKlantConfigurationProperties(
                        klantinteractiesApiUrl = mockServer.url(API_PATH).toUri(),
                        token = "SuperSecretToken1234",
                    ),
            )
        openKlant2Client = OpenKlant2KlantinteractiesClient(openklantModuleConfiguration.properties)
    }

    @AfterEach
    internal fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `should apply path to request`() =
        runTest {
            // when
            mockServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json"),
            )

            // given
            openKlant2Client.path<Partijen>().get()
            val request = mockServer.takeRequest()

            // then
            assertEquals("$API_PATH$PATH", request.requestUrl?.encodedPath)
        }

    @Test
    fun `get - should apply query parameters to request`() =
        runTest {
            // when
            val filters =
                listOf(
                    OpenKlant2PartijenFilters.PAGE to 1,
                    SOORT_PARTIJ to SoortPartij.PERSOON,
                    PARTIJ_IDENTIFICATOR_OBJECT_ID to "999990755",
                )
            mockServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json"),
            )

            // given
            openKlant2Client.path<Partijen>().get(filters)
            val request = mockServer.takeRequest()

            // then
            assertEquals("1", request.requestUrl?.queryParameter("page"))
            assertEquals("persoon", request.requestUrl?.queryParameter(SOORT_PARTIJ.toString()))
            assertEquals("999990755", request.requestUrl?.queryParameter(PARTIJ_IDENTIFICATOR_OBJECT_ID.toString()))
        }

    @Test
    fun `find - should return single`() =
        runTest {
            // when
            val filters =
                listOf(
                    OpenKlant2PartijenFilters.PAGE to 1,
                    SOORT_PARTIJ to SoortPartij.PERSOON,
                    PARTIJ_IDENTIFICATOR_OBJECT_ID to "999990755",
                )
            mockServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(TestHelper.Partijen.persoonPartijResponse),
            )

            // given
            val response = openKlant2Client.path<Partijen>().find(filters)

            // then
            assertNotNull(response)
            assertTrue(response is OpenKlant2Partij)
        }

    companion object {
        const val PATH = "/partijen"
        const val API_PATH = "/myapi/v1"
    }
}