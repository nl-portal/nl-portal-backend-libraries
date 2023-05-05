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
package com.ritense.portal.taak.graphql

import com.ritense.portal.gzac.objectsapi.domain.ObjectsApiObject
import com.ritense.portal.gzac.objectsapi.domain.ResultPage
import com.ritense.portal.taak.domain.ObjectsApiTaak
import com.ritense.portal.taak.domain.Taak

class TaakPage(
    number: Int,
    size: Int,
    content: List<Taak>,
    totalElements: Int,
) : Page<Taak>(number, size, content, totalElements) {

    companion object {
        fun fromResultPage(pageNumber: Int, pageSize: Int, resultPage: ResultPage<ObjectsApiObject<ObjectsApiTaak>>): TaakPage {
            return TaakPage(
                number = pageNumber,
                size = pageSize,
                content = resultPage.results.map { Taak.fromObjectsApiTask(it) },
                totalElements = resultPage.count
            )
        }
    }
}