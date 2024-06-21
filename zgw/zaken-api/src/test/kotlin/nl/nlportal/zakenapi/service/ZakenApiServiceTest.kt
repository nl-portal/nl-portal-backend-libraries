package nl.nlportal.zakenapi.service

import kotlinx.coroutines.test.runTest
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.DocumentStatus.DEFINITIEF
import nl.nlportal.documentenapi.domain.DocumentStatus.GEARCHIVEERD
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.GEHEIM
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.OPENBAAR
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.ZAAKVERTROUWELIJK
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.zakenapi.TestHelper.testDocument
import nl.nlportal.zakenapi.TestHelper.testZaakDocument
import nl.nlportal.zakenapi.client.ZaakDocumentenConfig
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.request.SearchZaakInformatieobjectenImpl
import nl.nlportal.zakenapi.client.request.ZakenInformatieobjectenImpl
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

class ZakenApiServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var zakenApiClient: ZakenApiClient
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var zakenApiService: ZakenApiService
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var objectsApiClient: ObjectsApiClient
    @Mock
    private lateinit var searchZaakInformatieobjectenImpl: SearchZaakInformatieobjectenImpl
    @Mock
    private lateinit var zakenInformatieobjectenImpl: ZakenInformatieobjectenImpl
    @Mock
    private lateinit var documentenApiService: DocumentenApiService
    private lateinit var zaakDocumentenConfig: ZaakDocumentenConfig

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        zaakDocumentenConfig = ZaakDocumentenConfig(
            vertrouwelijkheidsaanduidingWhitelist = listOf(
                OPENBAAR,
                ZAAKVERTROUWELIJK,
            )
        )

        zakenApiService = ZakenApiService(zakenApiClient, zaakDocumentenConfig, documentenApiService, objectsApiClient)
    }

    @Test
    fun `should only return documents with allowed confidentiality`() = runTest {

        val informatieObjecten: Array<Document> = arrayOf(
            testDocument.copy(
                status = GEARCHIVEERD,
                vertrouwelijkheidaanduiding = ZAAKVERTROUWELIJK
            ),
            testDocument.copy(
                status = DEFINITIEF,
                vertrouwelijkheidaanduiding = GEHEIM
            ),
            testDocument.copy(
                status = DEFINITIEF,
                vertrouwelijkheidaanduiding = OPENBAAR
            ),
        )
        val zaakDocumenten: List<ZaakDocument> = listOf(
            testZaakDocument,
            testZaakDocument,
            testZaakDocument,
            testZaakDocument,
        )

        // given
        doReturn(zakenInformatieobjectenImpl).whenever(zakenApiClient).zaakInformatieobjecten()
        doReturn(searchZaakInformatieobjectenImpl).whenever(zakenInformatieobjectenImpl).search()
        doReturn(searchZaakInformatieobjectenImpl).whenever(searchZaakInformatieobjectenImpl).forZaak(any<String>())
        doReturn(zaakDocumenten).wheneverBlocking(searchZaakInformatieobjectenImpl) { retrieve() }
        doReturn(testDocument, *informatieObjecten).wheneverBlocking(documentenApiService) { getDocument(any()) }

        // when
        val filteredDocuments =
            zakenApiService.getDocumenten("example.com")


        assertEquals(2, filteredDocuments.size)
    }
}