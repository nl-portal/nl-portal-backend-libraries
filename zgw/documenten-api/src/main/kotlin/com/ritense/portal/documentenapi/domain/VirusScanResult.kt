package com.ritense.portal.documentenapi.domain

import java.util.Collections

class VirusScanResult {
    var status: VirusScanStatus = VirusScanStatus.OK
    var foundViruses: Map<String, Collection<String>> = Collections.emptyMap()
}