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
package nl.nlportal.zgw.objectenapi.service

import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import java.net.URI

class ObjectenApiService(
    val objectsApiClient: ObjectsApiClient,
    val objectsApiClientConfig: ObjectsApiClientConfig
) {
    suspend inline fun <reified T> getObjects(
        objectSearchParameters: List<ObjectSearchParameter>,
        objectTypeUrl: String,
        pageNumber: Int,
        pageSize: Int
    ): ResultPage<ObjectsApiObject<T>> {
        return objectsApiClient.getObjects(
            objectSearchParameters = objectSearchParameters,
            objectTypeUrl = objectTypeUrl,
            page = pageNumber,
            pageSize = pageSize
        )
    }

    suspend inline fun <reified T> getObjectById(
        objectId: String
    ): ObjectsApiObject<T>? {
        return objectsApiClient.getObjectById(objectId)
    }

    suspend inline fun <reified T> getObjectByUrl(
        url: String
    ): ObjectsApiObject<T>? {
        val requestedObjectenApiHost = URI.create(url).host
        val configuredObjectenApiHost = objectsApiClientConfig.url.host
        if (!requestedObjectenApiHost.equals(configuredObjectenApiHost)) {
            throw IllegalArgumentException("Configured Objects API hostname does not match the requested object")
        }
        return objectsApiClient.getObjectByUrl(url)
    }
}