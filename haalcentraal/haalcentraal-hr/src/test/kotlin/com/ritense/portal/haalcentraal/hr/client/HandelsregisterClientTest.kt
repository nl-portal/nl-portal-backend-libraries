/*
 * Copyright 2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.haalcentraal.hr.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.ritense.portal.haalcentraal.client.HaalCentraalClientConfig
import com.ritense.portal.haalcentraal.client.HaalCentraalClientProvider
import com.ritense.portal.haalcentraal.hr.domain.MaatschappelijkeActiviteit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HandelsregisterClientTest {

    private lateinit var haalCentraalClientConfig: HaalCentraalClientConfig
    private lateinit var haalCentraalClientProvider: HaalCentraalClientProvider
    private lateinit var client: HandelsregisterClient
    private lateinit var server: MockWebServer
    private val kvkNummer = "90012768"

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        server.start()

        server.enqueue(
            MockResponse()
                .setBody(jacksonObjectMapper().writeValueAsString(MaatschappelijkeActiviteit(naam = "Test bedrijf")))
                .addHeader("Content-Type", "application/json"),
        )

        haalCentraalClientConfig = HaalCentraalClientConfig(url = server.url("/").toString())
        haalCentraalClientProvider = HaalCentraalClientProvider(haalCentraalClientConfig, null)
        client = HandelsregisterClient(haalCentraalClientProvider)
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should get bedrijf by without certificate`() {
        runBlocking {
            val bedrijf = client.getMaatschappelijkeActiviteit(kvkNummer, mock())

            assertThat(bedrijf).isNotNull
        }
    }
}