package nl.nlportal.core.autoconfiguration

import com.fasterxml.jackson.databind.ObjectMapper
import nl.nlportal.core.util.Mapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoreAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = ["objectMapper"])
    fun objectMapper(): ObjectMapper {
        return Mapper.get()
    }
}