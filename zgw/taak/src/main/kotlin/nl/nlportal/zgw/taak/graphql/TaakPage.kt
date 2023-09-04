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
package nl.nlportal.zgw.taak.graphql

import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.taak.domain.Taak
import nl.nlportal.zgw.taak.domain.TaakObject

class TaakPage(
    number: Int,
    size: Int,
    content: List<Taak>,
    results: List<Taak>,
    totalElements: Int
) : Page<Taak>(number, size, content, results, totalElements) {

    companion object {
        fun fromResultPage(pageNumber: Int, pageSize: Int, resultPage: ResultPage<ObjectsApiObject<TaakObject>>): TaakPage {
            val tasks = resultPage.results.map { Taak.fromObjectsApiTask(it) }

            return TaakPage(
                number = pageNumber,
                size = pageSize,
                content = tasks,
                results = tasks,
                totalElements = resultPage.count
            )
        }
    }
}