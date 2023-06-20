package com.ritense.portal.service

import com.ritense.portal.BaseIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class UploadServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var uploadService: UploadService

    lateinit var multipartFile: MultipartFile

    @BeforeEach
    fun setUp() {
        multipartFile = MockMultipartFile(
            "Test",
            ByteArray(100),
        )
    }

    @Test
    fun `Should upload to s3`() {
        uploadService.upload(multipartFile)
    }
}