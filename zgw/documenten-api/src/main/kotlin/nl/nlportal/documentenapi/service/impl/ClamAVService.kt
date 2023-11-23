package nl.nlportal.documentenapi.service.impl

import nl.nlportal.documentenapi.domain.VirusScanResult
import nl.nlportal.documentenapi.domain.VirusScanStatus
import nl.nlportal.documentenapi.service.VirusScanService
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class ClamAVService(
    private val clamAVClient: ClamavClient,
) : VirusScanService {

    override fun scan(content: Flux<DataBuffer>): VirusScanResult {
        return getInputStreamFromFluxDataBuffer(content).use {
            when (val scanResult = clamAVClient.scan(it)) {
                is ScanResult.OK -> {
                    VirusScanResult(VirusScanStatus.OK, mapOf())
                }
                is ScanResult.VirusFound -> {
                    VirusScanResult(VirusScanStatus.VIRUS_FOUND, scanResult.foundViruses)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun getInputStreamFromFluxDataBuffer(content: Flux<DataBuffer>): InputStream {
        val osPipe = PipedOutputStream()
        val isPipe = PipedInputStream(osPipe)
        DataBufferUtils.write(content, osPipe)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnComplete {
                osPipe.close()
            }
            .subscribe(DataBufferUtils.releaseConsumer())
        return isPipe
    }
}