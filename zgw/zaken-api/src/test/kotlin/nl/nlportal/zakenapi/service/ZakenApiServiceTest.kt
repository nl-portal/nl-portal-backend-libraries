package nl.nlportal.zakenapi.service

import kotlinx.coroutines.test.runTest
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.DocumentStatus
import nl.nlportal.documentenapi.domain.DocumentStatus.DEFINITIEF
import nl.nlportal.documentenapi.domain.DocumentStatus.GEARCHIVEERD
import nl.nlportal.documentenapi.domain.DocumentStatus.IN_BEWERKING
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.GEHEIM
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.OPENBAAR
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.ZAAKVERTROUWELIJK
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.zakenapi.TestHelper.testDocument
import nl.nlportal.zakenapi.TestHelper.testZaakDocument
import nl.nlportal.zakenapi.client.ZaakDocumentenConfig
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.domain.ZaakDocument
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZakenApiServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var zakenApiClient: ZakenApiClient

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var zakenApiService: ZakenApiService

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var objectsApiClient: ObjectsApiClient

    @Mock
    private lateinit var documentenApiService: DocumentenApiService
    private lateinit var zaakDocumentenConfig: ZaakDocumentenConfig

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        zaakDocumentenConfig =
            ZaakDocumentenConfig(
                vertrouwelijkheidsaanduidingWhitelist =
                    listOf(
                        OPENBAAR,
                        ZAAKVERTROUWELIJK,
                    ),
            )

        zakenApiService = ZakenApiService(zakenApiClient, zaakDocumentenConfig, documentenApiService, objectsApiClient)
    }

    @Test
    fun `should only return documents with whitelisted status and confidentiality`() =
        runTest {
            val informatieObjecten: Array<Document> =
                arrayOf(
                    testDocument.copy(
                        status = IN_BEWERKING,
                        vertrouwelijkheidaanduiding = ZAAKVERTROUWELIJK,
                    ),
                    testDocument.copy(
                        status = DEFINITIEF,
                        vertrouwelijkheidaanduiding = GEHEIM,
                    ),
                    testDocument.copy(
                        status = DEFINITIEF,
                        vertrouwelijkheidaanduiding = OPENBAAR,
                    ),
                )
            val zaakDocumenten: List<ZaakDocument> =
                listOf(
                    testZaakDocument,
                    testZaakDocument,
                    testZaakDocument,
                    testZaakDocument,
                )

            // given
            whenever(zakenApiClient.zaakInformatieobjecten().search().forZaak(any<String>()).retrieve())
                .thenReturn(zaakDocumenten)
            doReturn(testDocument, *informatieObjecten).wheneverBlocking(documentenApiService) { getDocument(any()) }

            // when
            val filteredDocuments =
                zakenApiService.getDocumenten("example.com")

            assertEquals(1, filteredDocuments.size)
            assertTrue { filteredDocuments.first().status in zaakDocumentenConfig.statusWhitelist }
            assertTrue { filteredDocuments.first().vertrouwelijkheidaanduiding in zaakDocumentenConfig.vertrouwelijkheidsaanduidingWhitelist }
        }

    @Test
    fun `should apply default whitelist to zaakdocumenten filter`() {
        val expectedStatuses: List<DocumentStatus> =
            listOf(
                DEFINITIEF,
                GEARCHIVEERD,
            )

        assertEquals(expectedStatuses, zaakDocumentenConfig.statusWhitelist)
    }
}