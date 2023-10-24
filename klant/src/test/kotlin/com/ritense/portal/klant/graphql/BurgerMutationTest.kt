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
package com.ritense.portal.klant.graphql

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import com.ritense.portal.klant.domain.klanten.KlantUpdate
import com.ritense.portal.klant.service.BurgerService
import nl.nlportal.klant.generiek.validation.GraphQlValidator
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import javax.validation.ValidationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.Authentication

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class BurgerMutationTest {

    @Mock
    lateinit var burgerService: BurgerService

    @Mock
    lateinit var environment: DataFetchingEnvironment

    @Mock
    lateinit var authentication: CommonGroundAuthentication

    @Mock
    lateinit var context: GraphQLContext

    private lateinit var burgerMutation: BurgerMutation

    @BeforeEach
    fun setup() {
        burgerMutation = BurgerMutation(burgerService, GraphQlValidator())
    }

    @Test
    fun `can update klant with valid phone number and email`() = runBlockingTest {
        Mockito.`when`(environment.graphQlContext).thenReturn(context)
        Mockito.`when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
        val klant = KlantUpdate(
            telefoonnummer = "0611111111",
            emailadres = "new@email.nl",
        )

        burgerMutation.updateBurgerProfiel(klant, environment)

        verify(burgerService).updateBurgerProfiel(klant, authentication)
    }

    @Test
    fun `can update klant with empty phone number and email`() = runBlockingTest {
        Mockito.`when`(environment.graphQlContext).thenReturn(context)
        Mockito.`when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
        val klant = KlantUpdate(
            telefoonnummer = "",
            emailadres = "",
        )

        burgerMutation.updateBurgerProfiel(klant, environment)

        verify(burgerService).updateBurgerProfiel(klant, authentication)
    }

    @Test
    fun `cant update klant with invalid phone number`() {
        val klant = KlantUpdate(
            telefoonnummer = "invalid-phone-number",
            emailadres = "",
        )
        val exception = Assertions.assertThrows(ValidationException::class.java) {
            runBlockingTest { burgerMutation.updateBurgerProfiel(klant, environment) }
        }
        assertThat(exception).hasMessage("Must be a valid phone number")
    }

    @Test
    fun `cant update klant with invalid email`() {
        val klant = KlantUpdate(
            telefoonnummer = "",
            emailadres = "invalid-email",
        )
        val exception = Assertions.assertThrows(ValidationException::class.java) {
            runBlockingTest { burgerMutation.updateBurgerProfiel(klant, environment) }
        }
        assertThat(exception).hasMessage("must be a well-formed email address")
    }
}