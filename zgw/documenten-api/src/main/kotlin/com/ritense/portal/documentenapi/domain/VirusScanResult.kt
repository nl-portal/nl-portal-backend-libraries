package com.ritense.portal.documentenapi.domain

sealed class VirusScanResult {
    object OK : VirusScanResult()
    data class VirusFound(val foundViruses: Map<String, Collection<String>>) : VirusScanResult()
}
