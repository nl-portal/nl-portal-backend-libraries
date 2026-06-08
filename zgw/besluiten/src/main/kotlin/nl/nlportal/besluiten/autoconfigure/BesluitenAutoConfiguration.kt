package nl.nlportal.besluiten.autoconfigure

import nl.nlportal.besluiten.client.BesluitenApiClient
import nl.nlportal.besluiten.client.BesluitenApiConfig
import nl.nlportal.besluiten.service.BesluitenService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(BesluitenApiConfig::class)
@ConditionalOnProperty(prefix = "nl-portal.config.besluitenapi", name = ["enabled"], havingValue = "true")
class BesluitenAutoConfiguration {
    @Bean
    fun besluitenApiClient(
        besluitenApiConfig: BesluitenApiConfig,
        webClientBuilder: WebClient.Builder,
    ): BesluitenApiClient {
        return BesluitenApiClient(besluitenApiConfig.properties, webClientBuilder)
    }

    @Bean
    fun besluitenService(besluitenApiClient: BesluitenApiClient): BesluitenService {
        return BesluitenService(besluitenApiClient)
    }
}
