package com.ritense.portal.core.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.security.headers", ignoreUnknownFields = true)
data class SecurityHeadersConfig(
       var contentSecurityPolicy: String?
)
