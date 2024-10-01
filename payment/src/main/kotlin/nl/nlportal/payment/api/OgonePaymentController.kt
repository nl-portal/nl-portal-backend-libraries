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
package nl.nlportal.payment.api

import nl.nlportal.payment.service.OgonePaymentService
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping(value = ["/api/public"])
class OgonePaymentController(
    private val ogonePaymentService: OgonePaymentService,
) {
    @GetMapping(value = ["/payment/ogone/postsale"])
    suspend fun postSale(httpServletRequest: ServerHttpRequest): ResponseEntity<Any> {
        try {
            return ResponseEntity.ok(
                ogonePaymentService.handlePostSale(
                    httpServletRequest,
                ),
            )
        } catch (exception: ResponseStatusException) {
            return ResponseEntity(exception.message, exception.statusCode)
        }
    }
}