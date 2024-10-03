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
package nl.nlportal.openklant.service

import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties
import nl.nlportal.openklant.client.OpenKlant2KlantinteractiesClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class OpenKlant2ServiceTest() {
    private lateinit var openklantModuleConfiguration: OpenKlantModuleConfiguration
    private lateinit var mockServer: MockWebServer
    private lateinit var openKlant2Client: OpenKlant2KlantinteractiesClient
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
                        klantinteractiesApiUrl = mockServer.url("/myapi/v1").toUri(),
                        token = "SuperSecretToken1234",
                    ),
            )
        openKlant2Client = OpenKlant2KlantinteractiesClient(openklantModuleConfiguration.properties)
    }

    @AfterEach
    internal fun tearDown() {
        mockServer.shutdown()
    }
}