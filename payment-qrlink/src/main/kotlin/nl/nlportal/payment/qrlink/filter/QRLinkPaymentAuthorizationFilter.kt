package nl.nlportal.payment.qrlink.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.ShaVersion
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class QRLinkPaymentAuthorizationFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void?> {
        val path = exchange.request.uri.path
        val queryParams = exchange.request.queryParams
        val apiKey = exchange.request.headers[HEADER_APIKEY]
        /*
        Only allowed access to QR Link Payment generate links with hash of indentifier as x-api-key header in the request
         */
        if (path.contains("/api/public/payment/qrlink/generate")) {
            if (apiKey == null) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to access endpoint.")
            }

            val identifier = queryParams.getFirst("identifier")
            if (identifier == null) {
                logger.error { "QRLinkPaymentAuthorizationFilter: No identifier found for $path" }
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No identifier found")
            }
            val hashIndentifier = hashIdentifier(identifier)

            if (hashIndentifier != apiKey[0]) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to access endpoint.")
            }
        }

        return chain.filter(exchange)
    }

    companion object {
        val logger = KotlinLogging.logger {}
        const val HEADER_APIKEY: String = "x-api-key"

        fun hashIdentifier(identifier: String): String =
            CoreUtils.createHash(
                input = identifier,
                shaVersion = ShaVersion.SHA512.version,
            )
    }
}
