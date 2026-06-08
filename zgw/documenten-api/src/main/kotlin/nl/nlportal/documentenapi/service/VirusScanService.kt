package nl.nlportal.documentenapi.service

import nl.nlportal.documentenapi.domain.VirusScanResult
import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux

interface VirusScanService {
    fun scan(content: ByteArray): VirusScanResult

    @Deprecated(
        message = "Flux is single-consumption — risks empty/partial scan when reused. Buffer content first and use scan(ByteArray).",
        replaceWith = ReplaceWith("scan(bufferedContent)"),
    )
    fun scan(content: Flux<DataBuffer>): VirusScanResult
}
