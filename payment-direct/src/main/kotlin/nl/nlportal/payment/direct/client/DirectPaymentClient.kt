/*
 * Copyright 2025 Ritense BV, the Netherlands.
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
package nl.nlportal.payment.direct.client

import com.onlinepayments.domain.CreateHostedCheckoutRequest
import com.onlinepayments.domain.CreateHostedCheckoutResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.handler.logging.LogLevel
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration.DirectPaymentProfile
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration.DirectPaymentProperties
import nl.nlportal.payment.direct.domain.DirectPaymentStatusResponse
import org.apache.commons.codec.binary.Base64
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class DirectPaymentClient(
    private val directPaymentProperties: DirectPaymentProperties,
    private val clientSslContextResolver: ClientSslContextResolver? = null,
) {
    val webClient: WebClient

    init {
        this.webClient =
            WebClient
                .builder()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient
                            .create()
                            .wiretap(
                                "reactor.netty.http.client.HttpClient",
                                LogLevel.TRACE,
                                AdvancedByteBufFormat.TEXTUAL,
                            ).let { client ->
                                var result = client
                                if (clientSslContextResolver != null) {
                                    directPaymentProperties.ssl?.let {
                                        val sslContext =
                                            clientSslContextResolver.resolve(
                                                it.key,
                                                it.trustedCertificate,
                                            )

                                        result = client.secure { builder -> builder.sslContext(sslContext) }

                                        logger.debug { "Client SSL context was set: private key=${it.key != null}, trusted certificate=${it.trustedCertificate != null}." }
                                    }
                                }
                                result
                            },
                    ),
                ).baseUrl(directPaymentProperties.url)
                .build()
    }

    suspend fun hostedCheckout(
        directPaymentProfile: DirectPaymentProfile,
        checkoutRequest: CreateHostedCheckoutRequest,
    ): CreateHostedCheckoutResponse {
        val endpointUrl = "/v2/${directPaymentProfile.pspId}/hostedcheckouts"
        val dateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        val hmacHeader =
            createHmacHeader(
                endpointUrl = endpointUrl,
                directPaymentProfile = directPaymentProfile,
                dateTime = dateTime,
                contentType = MediaType.APPLICATION_JSON.toString(),
                httpMethod = HttpMethod.POST,
            )

        return webClient
            .post()
            .uri(endpointUrl)
            .headers {
                it.add(HttpHeaders.AUTHORIZATION, hmacHeader)
                it.add(HttpHeaders.DATE, dateTime)
            }.contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(checkoutRequest)
            .retrieve()
            .awaitBody<CreateHostedCheckoutResponse>()
    }

    suspend fun hostedCheckoutStatus(
        directPaymentProfile: DirectPaymentProfile,
        hostedCheckoutId: String,
    ): DirectPaymentStatusResponse {
        val endpointUrl = "/v2/${directPaymentProfile.pspId}/hostedcheckouts/$hostedCheckoutId"
        val dateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        val hmacHeader =
            createHmacHeader(
                endpointUrl = endpointUrl,
                directPaymentProfile = directPaymentProfile,
                dateTime = dateTime,
                contentType = "",
                httpMethod = HttpMethod.GET,
            )

        return webClient
            .get()
            .uri(endpointUrl)
            .headers {
                it.add(HttpHeaders.AUTHORIZATION, hmacHeader)
                it.add(HttpHeaders.DATE, dateTime)
            }.retrieve()
            .awaitBody<DirectPaymentStatusResponse>()
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class, UnsupportedEncodingException::class)
    fun createHmacHeader(
        endpointUrl: String,
        directPaymentProfile: DirectPaymentProfile,
        dateTime: String,
        contentType: String,
        httpMethod: HttpMethod,
    ): String {
        val stringToHash = "${httpMethod}\n${contentType}\n$dateTime\n$endpointUrl\n"
        val sha256HMAC = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(directPaymentProfile.apiSecret.toByteArray(), "HmacSHA256")
        sha256HMAC.init(secretKey)
        val hash = Base64.encodeBase64String(sha256HMAC.doFinal(stringToHash.toByteArray()))
        return "GCS v1HMAC:" + directPaymentProfile.apiKey + ":" + hash
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}