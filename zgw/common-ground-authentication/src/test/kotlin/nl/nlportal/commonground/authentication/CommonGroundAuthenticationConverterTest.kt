/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.nlportal.commonground.authentication

import nl.nlportal.commonground.authentication.exception.UserTypeUnsupportedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
internal class CommonGroundAuthenticationConverterTest {
    @Mock
    lateinit var decoder: ReactiveJwtDecoder
    val keycloak = KeycloakConfig("bla", "bla_audience", Credentials("Bla"))
    lateinit var converter: CommonGroundAuthenticationConverter

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
        converter = spy(CommonGroundAuthenticationConverter(decoder, keycloak))
    }

    @Test
    fun `converter returns BurgerAuthentication when JWT has BSN`() {
        val jwt = JwtBuilder().aanvragerBsn("1234").buildJwt()
        val jwtString = JwtBuilder().aanvragerBsn("1234").buildJwtString()
        val tokenResponse = CommonGroundAuthenticationConverter.TokenResponse(jwtString)

        doReturn(Mono.just(jwt)).whenever(decoder).decode(tokenResponse.accessToken)
        doReturn(Mono.just(tokenResponse)).whenever(converter).tokenExchange(jwt)

        val authentication = converter.convert(jwt)

        assertTrue(authentication.block() is BurgerAuthentication)
    }

    @Test
    fun `converter returns BedrijfAuthentication when JWT has KvK nummer`() {
        val jwt = JwtBuilder().aanvragerKvk("1234").buildJwt()
        val jwtString = JwtBuilder().aanvragerKvk("1234").buildJwtString()
        val tokenResponse = CommonGroundAuthenticationConverter.TokenResponse(jwtString)

        doReturn(Mono.just(jwt)).whenever(decoder).decode(tokenResponse.accessToken)
        doReturn(Mono.just(tokenResponse)).whenever(converter).tokenExchange(jwt)

        val authentication = converter.convert(jwt)

        assertTrue(authentication.block() is BedrijfAuthentication)
    }

    @Test
    fun `converter throws exception when JWT has no KvK nummer or BSN`() {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "none")
                .claim("random", "1234")
                .build()
        val jwtString = JwtBuilder().buildJwtString()
        val tokenResponse = CommonGroundAuthenticationConverter.TokenResponse(jwtString)

        doReturn(Mono.just(jwt)).whenever(decoder).decode(tokenResponse.accessToken)
        doReturn(Mono.just(tokenResponse)).whenever(converter).tokenExchange(jwt)

        val exception =
            assertThrows(UserTypeUnsupportedException::class.java) {
                converter.convert(jwt).block()
            }
        assertEquals("User type not supported", exception.message)
    }
}