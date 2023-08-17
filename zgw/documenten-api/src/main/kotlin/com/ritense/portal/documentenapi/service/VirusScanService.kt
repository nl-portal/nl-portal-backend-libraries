package com.ritense.portal.documentenapi.service

import com.ritense.portal.documentenapi.domain.VirusScanResult
import java.io.InputStream

fun interface VirusScanService {
    fun scan(originalStream: InputStream): VirusScanResult
}