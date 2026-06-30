package nl.nlportal.zakenapi.service

import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.AuthenticationMachtigingsDienstService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.documentenapi.client.DocumentApisConfig.DocumentenApisConfigProperties
import nl.nlportal.documentenapi.client.DocumentApisConfig.DocumentenApisConfigProperties.DocumentApiConfig
import nl.nlportal.documentenapi.client.DocumentenApiClient
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.OPENBAAR
import nl.nlportal.documentenapi.domain.Vertrouwelijkheid.ZAAKVERTROUWELIJK
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.zakenapi.TestHelper.testDocument
import nl.nlportal.zakenapi.TestHelper.testZaakDocument
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.ZakenApiConfig.ZakenApiConfigProperties
import nl.nlportal.zakenapi.domain.ZaakRol
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory

class ZakenApiServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var zakenApiClient: ZakenApiClient

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var zakenApiService: ZakenApiService

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var objectsApiClient: ObjectsApiClient

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var documentenApiClient: DocumentenApiClient

    @Mock
    private lateinit var documentenApiService: DocumentenApiService
    private lateinit var zakenApiConfigProperties: ZakenApiConfigProperties
    private lateinit var documentApisConfig: DocumentApisConfig

    @Mock
    private lateinit var authenticationMachtigingsDienstService: AuthenticationMachtigingsDienstService

    @Mock
    private lateinit var authentication: CommonGroundAuthentication

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        zakenApiConfigProperties = ZakenApiConfigProperties().apply {
            url = "http://localhost:8080"
            clientId = "zaken-api-client-id"
            secret = "zaken-api-secret"
            zaakTypesIdsExcluded = emptyList()
        }

        val documentApiConfig = DocumentApiConfig().apply {
            url = "http://localhost:8001"
            clientId = "zaken-api-client-id"
            secret = "zaken-api-secret"
        }
        val documentApiConfigs = mapOf("openzaak" to documentApiConfig)
        documentApisConfig = DocumentApisConfig().apply {
            properties = DocumentenApisConfigProperties().apply {
                vertrouwelijkheidsaanduidingWhitelist =
                    listOf(
                        OPENBAAR,
                        ZAAKVERTROUWELIJK,
                    )
                configurations = mapOf("openzaak" to documentApiConfig)
            }

        }

        zakenApiService =
            ZakenApiService(
                zakenApiClient,
                zakenApiConfigProperties,
                documentenApiService,
                objectsApiClient,
                authenticationMachtigingsDienstService,
            )

        documentenApiService = DocumentenApiService(
            documentenApiClient = documentenApiClient,
            documentenApisConfigProperties = documentApisConfig.properties
        )
    }

    @Test
    fun `should return null when no roles found for user`() =
        runTest {
            val testRollen: List<ZaakRol> =
                emptyList()

            // given
            whenever(zakenApiClient.zaakInformatieobjecten().get(any()).retrieve())
                .thenReturn(testZaakDocument)

            whenever(
                zakenApiClient
                    .zaakRollen()
                    .search()
                    .forZaak(testZaakDocument.zaak)
                    .withAuthentication(authentication)
                    .retrieveAll(),
            )
                .thenReturn(testRollen)

            whenever(documentenApiService.getDocument(testZaakDocument.informatieobject))
                .doReturn(testDocument)
            whenever(documentenApiService.getDocumentContentStreaming(testZaakDocument.informatieobject))
                .doReturn(getTestFileContent())

            // when
            val (document, content) = zakenApiService.getZaakDocumentContent(testZaakDocument.uuid, authentication)

            // then
            assertNull(document)
            assertNull(content)
        }

    private fun getTestFileContent(): Flow<DataBuffer> {
        val bytes =
            File(TEST_TEXT_FILE)
                .readBytes()

        return flowOf(
            DefaultDataBufferFactory()
                .wrap(bytes),
        )
    }

    companion object {
        private const val TEST_TEXT_FILE = "src/test/resources/config/data/example-text-file.txt"
        private const val TEST_TEXT_FILE_CONTENT = "Test file content"
    }
}
