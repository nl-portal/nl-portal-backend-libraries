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
package com.ritense.portal.task.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.graphql.security.directive.IsUnauthenticated
import com.ritense.portal.task.service.TaskService

class PublicTaskQuery(val taskService: TaskService) : Query {

    @IsUnauthenticated
    @GraphQLDescription("find public task with id")
    fun findPublicTask(taskId: String): TaskInstance {
        return TaskInstance.from(taskService.findPublicTask(taskId))
    }
}