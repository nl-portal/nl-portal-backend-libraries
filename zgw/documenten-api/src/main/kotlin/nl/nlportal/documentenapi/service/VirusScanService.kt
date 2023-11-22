package nl.nlportal.documentenapi.service

import nl.nlportal.documentenapi.domain.VirusScanResult
import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux

fun interface VirusScanService {
    fun scan(content: Flux<DataBuffer>): VirusScanResult
}