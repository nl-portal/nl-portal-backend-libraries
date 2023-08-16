package com.ritense.portal.documentenapi.service.impl

import com.ritense.portal.documentenapi.domain.VirusScanResult
import com.ritense.portal.documentenapi.service.VirusScanService
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.InputStream

class ClamAVService(
        private val clamAVClient: ClamavClient
) : VirusScanService {

    override fun scan(originalStream: InputStream): VirusScanResult {
        val result = when (val scanResult = clamAVClient.scan(originalStream)) {
            is ScanResult.OK -> VirusScanResult.OK
            is ScanResult.VirusFound -> VirusScanResult.VirusFound(scanResult.foundViruses)
        }

        return result;
    }
}
