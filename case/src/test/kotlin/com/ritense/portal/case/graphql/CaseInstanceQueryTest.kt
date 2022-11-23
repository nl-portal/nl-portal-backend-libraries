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
package com.ritense.portal.case.graphql

import com.ritense.portal.case.BaseTest
import com.ritense.portal.case.service.CaseService
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.security.core.Authentication
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CaseInstanceQueryTest : BaseTest() {

    var environment = mock(DataFetchingEnvironment::class.java)
    var authentication = mock(Authentication::class.java)
    var caseService = mock(CaseService::class.java)
    var caseInstanceQuery = CaseInstanceQuery(caseService)
    val context = mock(GraphQLContext::class.java)
    var userId = "123"

    @Test
    fun shouldGetAllCaseInstancesInAscendingOrder() {
        val today = LocalDateTime.now()
        val yesterday = LocalDateTime.now().minusDays(1)

        `when`(caseService.getAllCases(userId)).thenReturn(listOf(case(today), case(yesterday)))
        `when`(environment.graphQlContext).thenReturn(context)
        `when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
        `when`(authentication.name).thenReturn(userId)

        val allCaseInstances = caseInstanceQuery.allCaseInstances(
            CaseInstanceOrdering(createdOn = Sort.ASC),
            environment
        )

        assertThat(allCaseInstances.first().createdOn).isEqualTo(yesterday.format(DateTimeFormatter.ISO_DATE_TIME))
        assertThat(allCaseInstances.last().createdOn).isEqualTo(today.format(DateTimeFormatter.ISO_DATE_TIME))
    }

    @Test
    fun shouldGetAllCaseInstancesInDescendingOrder() {
        val today = LocalDateTime.now()
        val yesterday = LocalDateTime.now().minusDays(1)

        `when`(caseService.getAllCases(userId)).thenReturn(listOf(case(today), case(yesterday)))
        `when`(environment.graphQlContext).thenReturn(context)
        `when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
        `when`(authentication.name).thenReturn(userId)

        val allCaseInstances = caseInstanceQuery.allCaseInstances(
            CaseInstanceOrdering(createdOn = Sort.DESC),
            environment
        )

        assertThat(allCaseInstances.first().createdOn).isEqualTo(today.format(DateTimeFormatter.ISO_DATE_TIME))
        assertThat(allCaseInstances.last().createdOn).isEqualTo(yesterday.format(DateTimeFormatter.ISO_DATE_TIME))
    }
}