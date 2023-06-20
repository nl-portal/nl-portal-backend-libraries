package com.ritense.portal.graphql

import com.ritense.portal.domain.UploadResult
import com.ritense.portal.service.UploadService
import org.springframework.web.multipart.MultipartFile

class UploadQuery(
    private val uploadService: UploadService
) {
    // @GraphQLDescription("Gets the profile for the user")
    suspend fun upload(multipartFile: MultipartFile): UploadResult {
        return uploadService.upload(multipartFile)
    }
}