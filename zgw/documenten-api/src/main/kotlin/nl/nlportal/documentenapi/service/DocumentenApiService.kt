package nl.nlportal.documentenapi.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import nl.nlportal.core.util.CoreUtils.extractId
import nl.nlportal.documentenapi.client.DocumentApisConfig.DocumentenApisConfigProperties
import nl.nlportal.documentenapi.client.DocumentenApiClient
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.DocumentContent
import nl.nlportal.documentenapi.domain.DocumentStatus
import nl.nlportal.documentenapi.domain.PostEnkelvoudiginformatieobjectRequest
import nl.nlportal.documentenapi.exceptions.MimeTypeDeniedException
import nl.nlportal.portal.authentication.domain.PortalAuthentication
import org.apache.tika.Tika
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.UUID

class DocumentenApiService(
    val documentenApiClient: DocumentenApiClient,
    val documentenApisConfigProperties: DocumentenApisConfigProperties,
) {
    suspend fun getDocument(
        documentId: UUID,
        documentApi: String,
    ): Document {
        return documentenApiClient.getDocument(documentId, documentApi)
    }

    suspend fun getDocument(documentUrl: String): Document {
        return documentenApiClient.getDocument(
            extractId(documentUrl),
            documentenApisConfigProperties.getConfigForDocumentUrl(documentUrl),
        )
    }

    suspend fun getDocumentContent(
        documentId: UUID,
        documentApi: String,
    ): DocumentContent {
        val documentContent = documentenApiClient.getDocumentContent(documentId, documentApi)
        return DocumentContent(Base64.getEncoder().encodeToString(documentContent))
    }

    fun getDocumentContentStreaming(
        documentId: UUID,
        documentApi: String,
    ): Flow<DataBuffer> {
        return documentenApiClient.getDocumentContentStream(documentId, documentApi)
    }

    fun getDocumentContentStreaming(informatieobejctUrl: String): Flow<DataBuffer> {
        val informatieObjectId = extractId(informatieobejctUrl)
        val documentenApi = documentenApisConfigProperties.getConfigForDocumentUrl(informatieobejctUrl)

        return documentenApiClient.getDocumentContentStream(informatieObjectId, documentenApi)
    }

    suspend fun uploadDocument(
        file: FilePart,
        documentApi: String,
        informatieobjecttype: String? = null,
    ): Document {
        val auteur =
            ReactiveSecurityContextHolder.getContext()
                .map { (it.authentication as PortalAuthentication).userId }
                .awaitSingleOrNull() ?: "valtimo"
        val documentenApiConfig = documentenApisConfigProperties.getConfig(documentApi)

        if (documentenApisConfigProperties.allowedMimeTypes.isNotEmpty()) {
            getInputStreamFromFluxDataBuffer(file.content()).use {
                val mediaType = Tika().detect(it).split(";")[0].trim()
                if (!documentenApisConfigProperties.allowedMimeTypes.contains(mediaType)) {
                    throw MimeTypeDeniedException("$mediaType is not allowed for uploads.")
                }
            }
        }

        return documentenApiClient.postDocument(
            PostEnkelvoudiginformatieobjectRequest(
                bronorganisatie = documentenApiConfig.rsin!!,
                creatiedatum = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                titel = file.filename(),
                auteur = auteur,
                status = DocumentStatus.DEFINITIEF,
                taal = "nld",
                bestandsnaam = file.filename(),
                indicatieGebruiksrecht = false,
                informatieobjecttype =
                    informatieobjecttype
                        .takeUnless {
                            it.isNullOrEmpty()
                        }
                        ?: documentenApiConfig.documentTypeUrl!!,
            ),
            file.content(),
            documentApi,
        )
    }

    @Throws(IOException::class)
    private fun getInputStreamFromFluxDataBuffer(content: Flux<DataBuffer>): InputStream {
        val osPipe = PipedOutputStream()
        val isPipe = PipedInputStream(osPipe)
        DataBufferUtils.write(content, osPipe)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnComplete {
                osPipe.close()
            }
            .subscribe(DataBufferUtils.releaseConsumer())
        return isPipe
    }
}