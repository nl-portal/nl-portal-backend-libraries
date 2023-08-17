package com.ritense.portal.documentenapi.service.impl

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.portal.documentenapi.domain.VirusScanStatus
import com.ritense.portal.documentenapi.service.VirusScanService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.InputStream

@ExperimentalCoroutinesApi
internal class ClamAVServiceTest {

    private var clamAVClient: ClamavClient = mock()
    private var clamAVService: VirusScanService = ClamAVService(clamAVClient)

    @Test
    fun `run scan should return OK`() = runBlocking {
        val originalStream: InputStream = mock()
        val content: Flux<DataBuffer> = mock()

        whenever(clamAVClient.scan(originalStream)).thenReturn(ScanResult.OK)
        val result = clamAVService.scan(content)
        assertEquals(VirusScanStatus.OK, result.status)
    }
}