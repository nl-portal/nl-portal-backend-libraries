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
package com.ritense.portal.gzac.objectsapi.task.graphql

import com.ritense.portal.gzac.objectsapi.domain.ObjectsApiObject
import com.ritense.portal.gzac.objectsapi.domain.ResultPage
import com.ritense.portal.gzac.objectsapi.task.domain.ObjectsApiTask
import com.ritense.portal.gzac.objectsapi.task.domain.Task

class TaskPage(
    number: Int,
    size: Int,
    content: List<Task>,
    totalElements: Int,
) : Page<Task>(number, size, content, totalElements) {

    companion object {
        fun fromResultPage(pageNumber: Int, pageSize: Int, resultPage: ResultPage<ObjectsApiObject<ObjectsApiTask>>): TaskPage {
            return TaskPage(
                number = pageNumber,
                size = pageSize,
                content = resultPage.results.map { Task.fromObjectsApiTask(it) },
                totalElements = resultPage.count
            )
        }
    }
}