package com.ritense.portal.domain

import com.amazonaws.services.s3.model.PutObjectResult
import java.util.UUID

data class UploadResult(

    val id: UUID,

    val bucketNAme: String,

    val putObjectResult: PutObjectResult?
)