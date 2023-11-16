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
package nl.nlportal.messaging.autoconfigure

import nl.nlportal.messaging.out.PortalMessage
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.function.Supplier

@AutoConfiguration
class MessagingAutoConfiguration {

    @Bean
    fun sink(): Sinks.Many<PortalMessage> {
        return Sinks.many().multicast().onBackpressureBuffer()
    }

    // Supplier
    @Bean
    fun portalEventSupplier(sink: Sinks.Many<PortalMessage>): Supplier<Flux<Message<PortalMessage>?>> = Supplier {
        sink.asFlux().map { e ->
            MessageBuilder
                .withPayload(e)
                .setHeader("spring.cloud.stream.sendto.destination", e.destination)
                .build()
        }
    }
}