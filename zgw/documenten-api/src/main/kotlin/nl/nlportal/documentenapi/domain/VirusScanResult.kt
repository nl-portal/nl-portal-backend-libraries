package nl.nlportal.documentenapi.domain

class VirusScanResult(
    val status: VirusScanStatus,
    val foundViruses: Map<String, Collection<String>>,
)