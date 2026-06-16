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
package nl.nlportal.payment.qrlink.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.ShaVersion
import nl.nlportal.payment.qrlink.autoconfiguratie.QRLinkPaymentModuleConfiguration
import nl.nlportal.payment.qrlink.autoconfiguratie.QRLinkPaymentModuleConfiguration.QRLinkPaymentProperties
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class QRLinkPaymentAuthorizationFilter(
    val qrLinkPaymentProperties: QRLinkPaymentProperties,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void?> {
        val path = exchange.request.uri.path
        val apiKey = exchange.request.headers[HEADER_APIKEY]
        val identifier = exchange.request.queryParams.getFirst("identifier")
        /*
        Only allowed access to QR Link Payment generate links with hash of identifier as x-api-key header in the request
         */
        if (path.contains("/api/public/payment/qrlink/generate")) {
            /*
            1. check is api key header or identifier query string parameter are present
             */
            if (apiKey == null || identifier == null) {
                logger.error { "QRLinkPaymentAuthorizationFilter: No apikey of identifier found for $path" }
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to access endpoint.")
            }
            /*
            2. check is api key header from header matched the configured api key
             */
            if (qrLinkPaymentProperties.getConfiguration(identifier)?.apiKey != apiKey[0]) {
                logger.error { "QRLinkPaymentAuthorizationFilter: No match for apikey $apiKey" }
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to access endpoint.")
            }
        }

        return chain.filter(exchange)
    }

    companion object {
        val logger = KotlinLogging.logger {}
        const val HEADER_APIKEY: String = "x-api-key"
    }
}