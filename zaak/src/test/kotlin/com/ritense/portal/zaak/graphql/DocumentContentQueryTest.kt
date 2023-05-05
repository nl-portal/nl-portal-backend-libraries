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
package com.ritense.portal.zaak.graphql

import com.ritense.portal.zaak.service.ZaakService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.UUID

@ExperimentalCoroutinesApi
internal class DocumentContentQueryTest {

    var zaakService = mock(ZaakService::class.java)
    var documentContentQuery = DocumentContentQuery(zaakService)

    @Test
    fun getDocumentContent() = runBlockingTest {
        val documentId = UUID.randomUUID()
        documentContentQuery.getDocumentContent(documentId)
        verify(zaakService).getDocumentContent(documentId)
    }
}