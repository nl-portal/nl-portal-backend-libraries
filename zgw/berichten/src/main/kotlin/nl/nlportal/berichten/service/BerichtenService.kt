/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.service

import nl.nlportal.berichten.autoconfigure.BerichtenConfigurationProperties
import nl.nlportal.berichten.domain.Bericht
import nl.nlportal.berichten.graphql.BerichtenPage
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService

class BerichtenService(
    private val objectenApiService: ObjectenApiService,
    private val berichtenConfigurationProperties: BerichtenConfigurationProperties,
) {
    suspend fun getBerichtenPage(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
    ): BerichtenPage {
        val results =
            objectenApiService
                .getObjects<Bericht>(
                    objectSearchParameters =
                        listOf(
                            ObjectSearchParameter("identificatie__type", Comparator.EQUAL_TO, authentication.userType),
                            ObjectSearchParameter("identificatie__value", Comparator.EQUAL_TO, authentication.userId),
                        ),
                    objectTypeUrl =
                        berichtenConfigurationProperties.berichtObjectTypeUrl,
                    pageNumber = pageNumber,
                    pageSize = pageSize,
                )

        return results.toBerichtenPage()
    }

    private fun ResultPage<ObjectsApiObject<Bericht>>.toBerichtenPage(
        pageNumber: Int = 1,
        pageSize: Int = 20,
    ): BerichtenPage {
        val berichten = results.map { it.record.data }

        return BerichtenPage(
            number = pageNumber,
            size = pageSize,
            content = berichten,
            totalElements = count,
        )
    }
}