package nl.nlportal.idtokenauthentication.autoconfigure

import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IdTokenGeneratorAutoConfiguration {

    @Bean
    fun idTokenGenerator(): IdTokenGenerator {
        return IdTokenGenerator()
    }
}