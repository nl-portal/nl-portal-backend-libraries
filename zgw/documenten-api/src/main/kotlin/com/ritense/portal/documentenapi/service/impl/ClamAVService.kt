package com.ritense.portal.documentenapi.service.impl

import com.ritense.portal.documentenapi.domain.VirusScanResult
import com.ritense.portal.documentenapi.domain.VirusScanStatus
import com.ritense.portal.documentenapi.service.VirusScanService
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.InputStream

class ClamAVService(
        private val clamAVClient: ClamavClient
) : VirusScanService {

    override fun scan(originalStream: InputStream): VirusScanResult {
        val result = VirusScanResult()
        when (val scanResult = clamAVClient.scan(originalStream)) {
            is ScanResult.OK -> result.status = VirusScanStatus.OK
            is ScanResult.VirusFound -> {
                result.status = VirusScanStatus.VIRUS_FOUND
                result.foundViruses = scanResult.foundViruses
            }
        }

        return result;
    }
}
