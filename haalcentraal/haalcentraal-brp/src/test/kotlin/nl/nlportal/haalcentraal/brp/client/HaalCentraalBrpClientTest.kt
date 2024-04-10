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
package nl.nlportal.haalcentraal.brp.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import nl.nlportal.commonground.authentication.JwtBuilder
import nl.nlportal.core.ssl.ClientKey
import nl.nlportal.core.ssl.Ssl
import nl.nlportal.core.ssl.StringClientSslContextResolver
import nl.nlportal.haalcentraal.brp.domain.persoon.Persoon
import nl.nlportal.haalcentraal.brp.domain.persoon.PersoonNaam
import nl.nlportal.haalcentraal.client.HaalCentraalClientConfig
import nl.nlportal.haalcentraal.client.HaalCentraalClientProvider
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HaalCentraalBrpClientTest {
    private lateinit var haalCentraalClientConfig: HaalCentraalClientConfig
    private lateinit var server: MockWebServer
    private val bsn = "0123456789"
    private val persoon = Persoon(burgerservicenummer = bsn, naam = PersoonNaam(voornamen = "John", geslachtsnaam = "Doe"))

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        server.start()

        server.enqueue(
            MockResponse()
                .setBody(jacksonObjectMapper().writeValueAsString(persoon))
                .addHeader("Content-Type", "application/json"),
        )

        haalCentraalClientConfig = HaalCentraalClientConfig(url = server.url("/").toString())
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should get person by without certificate`() {
        val provider = HaalCentraalClientProvider(haalCentraalClientConfig, null)
        val client = HaalCentraalBrpClient(provider)
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        runBlocking {
            val persoon = client.getPersoon(bsn, authentication)
            assertThat(persoon).isNotNull
        }
    }

    @Test
    fun `should get person by with certificate`() {
        // Create the root for client and server to trust. We could also use different roots for each!
        val rootCertificate: HeldCertificate =
            HeldCertificate.Builder()
                .commonName("myRoot")
                .certificateAuthority(0)
                .build()

        // Create a server certificate and a server that uses it.
        val serverCertificate: HeldCertificate =
            HeldCertificate.Builder()
                .commonName("myServer")
                .addSubjectAlternativeName(server.hostName)
                .signedBy(rootCertificate)
                .build()
        val serverCertificates: HandshakeCertificates =
            HandshakeCertificates.Builder()
                .addTrustedCertificate(rootCertificate.certificate)
                .heldCertificate(serverCertificate)
                .build()
        server.useHttps(serverCertificates.sslSocketFactory(), false)
        server.requireClientAuth()

        // Create a client certificate and a client that uses it.
        val clientCertificate: HeldCertificate =
            HeldCertificate.Builder()
                .commonName("myClient")
                .organizationalUnit("myOrganisation")
                .signedBy(rootCertificate)
                .build()

        val haalCentraalClientConfig =
            haalCentraalClientConfig.copy(
                url = server.url("/").toString(),
                ssl =
                    Ssl(
                        key =
                            ClientKey(
                                certChain = "${clientCertificate.certificatePem()}\n${rootCertificate.certificatePem()}",
                                key = clientCertificate.privateKeyPkcs8Pem(),
                            ),
                        trustedCertificate = rootCertificate.certificatePem(),
                    ),
            )

        val provider = HaalCentraalClientProvider(haalCentraalClientConfig, StringClientSslContextResolver())
        val client = HaalCentraalBrpClient(provider)
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        runBlocking {
            val persoon = client.getPersoon(bsn, authentication)
            assertThat(persoon).isNotNull

            val recordedRequest = server.takeRequest()
            val peerPrincipal = recordedRequest.handshake!!.peerPrincipal.toString()
            assertThat(peerPrincipal).contains("CN=myClient", "OU=myOrganisation")
        }
    }
}