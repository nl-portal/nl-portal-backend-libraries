package com.ritense.portal.documentenapi.service.impl

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.portal.documentenapi.domain.VirusScanResult
import com.ritense.portal.documentenapi.service.VirusScanService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.InputStream
import java.util.*

@ExperimentalCoroutinesApi
internal class ClamAVServiceTest {

    private var clamAVClient: ClamavClient = mock()
    private var clamAVService: VirusScanService = ClamAVService(clamAVClient)

    @Test
    fun `run scan should return OK` () = runTest {
        val originalStream: InputStream = mock()
        whenever(clamAVClient.scan(originalStream)).thenReturn(ScanResult.OK)
        val result = clamAVService.scan(originalStream)
        assertEquals(VirusScanResult.OK, result)
    }

    @Test
    fun `scan should return VirusFound`()  = runTest{
        val originalStream: InputStream = mock()
        whenever(clamAVClient.scan(originalStream)).thenReturn(
                ScanResult.VirusFound(
                        Collections.singletonMap("test", Collections.singleton("test"))
                )
        )

        val result = clamAVService.scan(originalStream)
        assertEquals(VirusScanResult.VirusFound(
                Collections.singletonMap("test", Collections.singleton("test")))
                , result)
    }
}
