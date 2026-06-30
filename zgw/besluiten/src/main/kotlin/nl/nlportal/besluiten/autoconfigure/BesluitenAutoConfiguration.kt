package nl.nlportal.besluiten.autoconfigure

import nl.nlportal.besluiten.client.BesluitenApiClient
import nl.nlportal.besluiten.client.BesluitenApiConfig
import nl.nlportal.besluiten.graphql.BesluitenQuery
import nl.nlportal.besluiten.security.config.BesluitDocumentResourceHttpSecurityConfigurer
import nl.nlportal.besluiten.service.BesluitenService
import nl.nlportal.besluiten.web.rest.BesluitDocumentResource
import nl.nlportal.catalogiapi.service.CatalogiApiService
import nl.nlportal.core.security.config.HttpSecurityConfigurer
import nl.nlportal.documentenapi.service.DocumentenApiService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(BesluitenApiConfig::class)
@ConditionalOnProperty(prefix = "nl-portal.config", name = ["besluitenapi.enabled", "documentenapis.enabled"], havingValue = "true")
class BesluitenAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(BesluitenApiClient::class)
    fun besluitenApiClient(
        besluitenApiConfig: BesluitenApiConfig,
        webClientBuilder: WebClient.Builder,
    ): BesluitenApiClient {
        return BesluitenApiClient(besluitenApiConfig.properties, webClientBuilder)
    }

    @Bean
    @ConditionalOnMissingBean(BesluitenService::class)
    fun besluitenService(
        besluitenApiClient: BesluitenApiClient,
        documentenApiService: DocumentenApiService,
    ): BesluitenService {
        return BesluitenService(
            besluitenApiClient = besluitenApiClient,
            documentenApiService = documentenApiService
        )
    }

    @Bean
    @ConditionalOnMissingBean(BesluitenQuery::class)
    fun besluitenQuery(
        besluitenService: BesluitenService,
        catalogiApiService: CatalogiApiService
    ): BesluitenQuery = BesluitenQuery(
        besluitenService = besluitenService,
        catalogiApiService = catalogiApiService
    )

    @Bean
    @ConditionalOnMissingBean(BesluitDocumentResource::class)
    fun besluitDocumentResource(
        besluitenService: BesluitenService,
    ): BesluitDocumentResource = BesluitDocumentResource(besluitenService)

    @Bean
    @ConditionalOnMissingBean(BesluitDocumentResourceHttpSecurityConfigurer::class)
    fun besluitDocumentResourceHttpSecurityConfigurer(): HttpSecurityConfigurer = BesluitDocumentResourceHttpSecurityConfigurer()
}
