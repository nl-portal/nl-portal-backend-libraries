package com.ritense.portal.documentenapi.autoconfigure

import com.ritense.portal.documentenapi.client.ClamAVVirusScanConfig
import com.ritense.portal.documentenapi.service.VirusScanService
import com.ritense.portal.documentenapi.service.impl.ClamAVService
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.capybara.clamav.ClamavClient

@Configuration
@ConditionalOnProperty("valtimo.zgw.documentenapi.virusscan.clamav", matchIfMissing = false)
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