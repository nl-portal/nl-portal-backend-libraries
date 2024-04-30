package org.example.nl.nlportal.portal.authentication.domain

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class PortalAuthentication(
    val jwt: Jwt,
    authorities: Collection<GrantedAuthority>?,
) : JwtAuthenticationToken(jwt, authorities) {
}