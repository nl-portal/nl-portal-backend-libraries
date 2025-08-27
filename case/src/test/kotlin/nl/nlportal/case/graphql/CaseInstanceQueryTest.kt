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
package nl.nlportal.case.graphql

import graphql.GraphQLContext
import nl.nlportal.case.BaseTest
import nl.nlportal.case.service.CaseService
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.JwtBuilder

class CaseInstanceQueryTest : BaseTest() {
    var caseService = mock(CaseService::class.java)
    var caseInstanceQuery = CaseInstanceQuery(caseService)
    val context = mock(GraphQLContext::class.java)
    var userId = "123"

    // @Test
    fun shouldGetAllCaseInstancesInAscendingOrder() {
        val today = LocalDateTime.now()
        val yesterday = LocalDateTime.now().minusDays(1)

        `when`(caseService.getAllCases(userId)).thenReturn(listOf(case(today), case(yesterday)))

        val jwt = JwtBuilder().aanvragerBsn("1234").gemachtigdeBsn("5678").buildJwt()
        val allCaseInstances =
            caseInstanceQuery.allCaseInstances(
                CaseInstanceOrdering(createdOn = Sort.ASC),
                BurgerAuthentication(jwt, emptyList()),
            )

        assertThat(allCaseInstances.first().createdOn).isEqualTo(yesterday.format(DateTimeFormatter.ISO_DATE_TIME))
        assertThat(allCaseInstances.last().createdOn).isEqualTo(today.format(DateTimeFormatter.ISO_DATE_TIME))
    }

    // @Test
    fun shouldGetAllCaseInstancesInDescendingOrder() {
        val today = LocalDateTime.now()
        val yesterday = LocalDateTime.now().minusDays(1)

        `when`(caseService.getAllCases(userId)).thenReturn(listOf(case(today), case(yesterday)))
        val jwt = JwtBuilder().aanvragerBsn("1234").gemachtigdeBsn("5678").buildJwt()
        val allCaseInstances =
            caseInstanceQuery.allCaseInstances(
                CaseInstanceOrdering(createdOn = Sort.DESC),
                BurgerAuthentication(jwt, emptyList()),
            )

        assertThat(allCaseInstances.first().createdOn).isEqualTo(today.format(DateTimeFormatter.ISO_DATE_TIME))
        assertThat(allCaseInstances.last().createdOn).isEqualTo(yesterday.format(DateTimeFormatter.ISO_DATE_TIME))
    }
}