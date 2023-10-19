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
package com.ritense.portal.documentenapi.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "valtimo.zgw.documentenapis")
class DocumentApisConfig {
    var documentapis: MutableList<DocumentApiConfig> = mutableListOf()

    fun getDefault(): DocumentApiConfig {
        return documentapis.filter { documentenApiConfig -> documentenApiConfig.default }.get(0)
    }

    fun getConfig(documentApi: String = ""): DocumentApiConfig {
        return documentapis.filter({ documentenApiConfig -> documentenApiConfig.url.contains(documentApi) && !documentApi.isNullOrEmpty() }).getOrElse(0, { getDefault() })
    }
}

class DocumentApiConfig {
    var default: Boolean = true
    lateinit var url: String
    lateinit var clientId: String
    lateinit var secret: String
    lateinit var rsin: String
    lateinit var documentTypeUrl: String

/*    constructor(default: Boolean, url: String, clientId: String, secret: String, rsin: String, documentTypeUrl: String) {
        this.default = default
        this.url = url
        this.clientId = clientId
        this.secret = secret
        this.rsin = rsin
        this.documentTypeUrl = documentTypeUrl
    }*/
}