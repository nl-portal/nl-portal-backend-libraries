package nl.nlportal.klant.generiek.client

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.idtokenauthentication.service.IdTokenGenerator
import io.netty.handler.logging.LogLevel
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class OpenKlantClientProvider(
    private val openKlantClientConfig: OpenKlantClientConfig,
    private val idTokenGenerator: IdTokenGenerator
) {
    fun webClient(authentication: CommonGroundAuthentication): WebClient {
        val token = idTokenGenerator.generateToken(
            openKlantClientConfig.secret,
            openKlantClientConfig.clientId,
            authentication.getUserId(),
            authentication.getUserRepresentation()
        )

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL
                    )
                )
            )
            .baseUrl(openKlantClientConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}