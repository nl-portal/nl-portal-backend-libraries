package nl.nlportal.berichten.autoconfigure

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    BerichtenConfigurationProperties::class,
)
class BerichtenAutoConfiguration