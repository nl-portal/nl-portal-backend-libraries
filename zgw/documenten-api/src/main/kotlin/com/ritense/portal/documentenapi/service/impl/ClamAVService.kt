package com.ritense.portal.documentenapi.service.impl

import com.ritense.portal.documentenapi.domain.VirusScanResult
import com.ritense.portal.documentenapi.domain.VirusScanStatus
import com.ritense.portal.documentenapi.service.VirusScanService
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import reactor.core.publisher.Flux
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class ClamAVService(
    private val clamAVClient: ClamavClient
) : VirusScanService {

    override fun scan(content: Flux<DataBuffer>): VirusScanResult {
        val result = VirusScanResult()
        val inputStream = dataBufferToInputStream(content)
        when (val scanResult = clamAVClient.scan(inputStream)) {
            is ScanResult.OK -> result.status = VirusScanStatus.OK
            is ScanResult.VirusFound -> {
                result.status = VirusScanStatus.VIRUS_FOUND
                result.foundViruses = scanResult.foundViruses
            }
        }

        return result
    }

    private fun dataBufferToInputStream(content: Flux<DataBuffer>): InputStream {
        val osPipe = PipedOutputStream()
        val isPipe = PipedInputStream(osPipe)
        DataBufferUtils.write(content, osPipe)
            .subscribe(DataBufferUtils.releaseConsumer())

        return isPipe
    }
}