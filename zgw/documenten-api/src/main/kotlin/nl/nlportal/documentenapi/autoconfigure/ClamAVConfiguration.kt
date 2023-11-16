package nl.nlportal.documentenapi.autoconfigure

import nl.nlportal.documentenapi.client.ClamAVVirusScanConfig
import nl.nlportal.documentenapi.service.VirusScanService
import nl.nlportal.documentenapi.service.impl.ClamAVService
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.capybara.clamav.ClamavClient

@Configuration
@ConditionalOnProperty(prefix = "nl-portal.zgw.documentenapis.virusscan.clamav", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(ClamAVVirusScanConfig::class)
class ClamAVConfiguration {

    @Bean
    fun clamAVVirusScanConfig(): ClamAVVirusScanConfig {
        return ClamAVVirusScanConfig()
    }

    @Bean
    fun clamAVClient(
        clamAVVirusScanConfig: ClamAVVirusScanConfig,
    ): ClamavClient {
        logger.info("ClamAV virusscan is loaded with host: {} and port: {}", clamAVVirusScanConfig.hostName, clamAVVirusScanConfig.port)
        return ClamavClient(
            clamAVVirusScanConfig.hostName,
            clamAVVirusScanConfig.port,
        )
    }

    @Bean
    @ConditionalOnMissingBean(VirusScanService::class)
    fun virusScanService(
        clamAVClient: ClamavClient,
    ): VirusScanService {
        return ClamAVService(clamAVClient)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}