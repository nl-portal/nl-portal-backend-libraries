package com.ritense.portal.autoconfigure

import com.ritense.portal.service.UploadService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UploadService::class)
    fun uploadService(): UploadService {
        return UploadService()
    }
}