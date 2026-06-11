package nl.nlportal.idtokenauthentication.autoconfigure

import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@AutoConfiguration
class IdTokenGeneratorAutoConfiguration {

    @Bean
    fun idTokenGenerator(): IdTokenGenerator {
        return IdTokenGenerator()
    }
}
