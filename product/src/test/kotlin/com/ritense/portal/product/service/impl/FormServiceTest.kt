/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.product.service.impl

import com.nhaarman.mockitokotlin2.verify
import com.ritense.portal.product.client.OpenFormulierenClient
import com.ritense.portal.product.domain.Form
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.UUID

@ExperimentalCoroutinesApi
class FormServiceTest {

    val openFormulierenClient = mock(OpenFormulierenClient::class.java)
    val formService = FormService(openFormulierenClient)

    @Test
    fun `getForms calls client and gets forms`() = runBlockingTest {
        val formId = UUID.randomUUID()
        `when`(openFormulierenClient.getForms()).thenReturn(
            listOf(
                Form(
                    formId,
                    "name",
                    true,
                    true
                )
            )
        )

        val forms = formService.getForms()

        assertEquals(1, forms.size)
        assertEquals(formId, forms[0].uuid)
        assertEquals("name", forms[0].name)

        verify(openFormulierenClient).getForms()
    }

    @Test
    fun `getForms filters inactive forms`() = runBlockingTest {
        val formId = UUID.randomUUID()
        `when`(openFormulierenClient.getForms()).thenReturn(
            listOf(
                Form(
                    formId,
                    "name",
                    false,
                    true
                )
            )
        )

        val forms = formService.getForms()

        verify(openFormulierenClient).getForms()

        assertEquals(0, forms.size)
    }

    @Test
    fun `getForms filters forms without authentication`() = runBlockingTest {
        val formId = UUID.randomUUID()
        `when`(openFormulierenClient.getForms()).thenReturn(
            listOf(
                Form(
                    formId,
                    "name",
                    true,
                    false
                )
            )
        )

        val forms = formService.getForms()

        verify(openFormulierenClient).getForms()

        assertEquals(0, forms.size)
    }
}