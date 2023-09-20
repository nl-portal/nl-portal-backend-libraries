package com.ritense.portal.documentenapi.service

import com.ritense.portal.documentenapi.domain.VirusScanResult
import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux

fun interface VirusScanService {
    fun scan(content: Flux<DataBuffer>): VirusScanResult
}