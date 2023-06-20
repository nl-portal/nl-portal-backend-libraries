package com.ritense.portal.service

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.DefaultAwsRegionProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.ritense.portal.domain.UploadResult
import java.io.File
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UploadService {

    fun upload(multipartFile: MultipartFile): UploadResult {
        val configuredMaxSize = 5000000L // get from configuration aws max size is 5gb
        if (multipartFile.size > configuredMaxSize) {
            throw Error("filesize is to big. Please dont upload a file bigger than ${configuredMaxSize / 1000}MB")
        }

        val bucketName = "nl-portal" // from app.yaml

        val credentials = DefaultAWSCredentialsProviderChain() // pw and username from app.yaml

        val region = DefaultAwsRegionProviderChain()

        val endpointConfiguration = EndpointConfiguration(
            "http://localhost.localstack.cloud:4568",
            "eu-central-1"
        )

        val client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(credentials)
            .withEndpointConfiguration(endpointConfiguration)// needs to be configured in app.yaml
            .build()

        val tempFile = File("src/main/resources/temp/helloworld.txt")

        multipartFile.transferTo(tempFile)

        val fileKeyUUID = UUID.randomUUID()

        val result = client.putObject(
            bucketName,
            fileKeyUUID.toString(),
            File("src/main/resources/temp/helloworld.txt"),
        )

        tempFile.delete()

        return UploadResult(
            fileKeyUUID,
            bucketName,
            result,
        )
    }
}