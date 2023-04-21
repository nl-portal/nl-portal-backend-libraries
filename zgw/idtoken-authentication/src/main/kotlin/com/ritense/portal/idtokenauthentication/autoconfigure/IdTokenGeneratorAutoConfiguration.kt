package com.ritense.portal.idtokenauthentication.autoconfigure

import com.ritense.portal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IdTokenGeneratorAutoConfiguration {

    @Bean
    fun idTokenGenerator(): IdTokenGenerator {
        return IdTokenGenerator()
    }

}