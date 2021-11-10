package com.nolions.ffmpegman.unit

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

object AWSS3Unit {
    private lateinit var s3Client: S3Client
    lateinit var bucket: String

    fun init(id: String, key: String, bucket: String? = null) {
        val credentials = credentials(id, key)

        this.s3Client = s3Client(credentials)

        bucket?.let {
            this.bucket = it
        }
    }

    private fun credentials(id: String, key: String) = AwsBasicCredentials.create(id, key)

    private fun s3Client(credentials: AwsBasicCredentials) = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build()

    fun putObject(fileName: String, filePath: String): Boolean {
        return try {
            val objectRequest = PutObjectRequest.builder()
                .bucket(this.bucket)
                .key(fileName)
                .build()

            s3Client.putObject(objectRequest, RequestBody.fromFile(File(filePath)))

            true
        } catch (e: Exception) {
            println("putObject fail, err:${e.message}")
            false
        }

    }
}