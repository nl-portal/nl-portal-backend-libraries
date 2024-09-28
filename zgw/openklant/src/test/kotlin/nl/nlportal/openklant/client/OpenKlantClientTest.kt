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
package nl.nlportal.openklant.client

import kotlinx.coroutines.test.runTest
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.awaitBodilessEntity

class OpenKlantClientTest {
    private lateinit var openklantModuleConfiguration: OpenKlantModuleConfiguration
    private lateinit var mockServer: MockWebServer
    private lateinit var openKlant2Client: OpenKlant2Client
    private lateinit var hostUrl: String
    private lateinit var apiUrl: String

    @BeforeEach
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()

        hostUrl = "http://${mockServer.hostName}:${mockServer.port}/"
        apiUrl = "http://${mockServer.hostName}:${mockServer.port}/myapi/v1"
        openklantModuleConfiguration =
            OpenKlantModuleConfiguration(
                enabled = true,
                properties =
                    OpenKlantConfigurationProperties(
                        url = mockServer.url("/myapi/v1").toUri(),
                        token = "SuperSecretToken1234",
                    ),
            )
        openKlant2Client = OpenKlant2Client(openklantModuleConfiguration.properties)
    }

    @AfterEach
    internal fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `should provide configured webclient`() =
        runTest {
            // when
            mockServer.enqueue(MockResponse().setResponseCode(200))

            // given
            openKlant2Client.webClient().get().uri("mypath").retrieve().awaitBodilessEntity()
            val request = mockServer.takeRequest()

            // then
            assertEquals("${apiUrl}mypath", request.requestUrl.toString())
            assertEquals("Token SuperSecretToken1234", request.getHeader("Authorization"))
            assertEquals("EPSG:4326", request.getHeader("Accept-Crs"))
            assertEquals("EPSG:4326", request.getHeader("Content-Crs"))
        }
}