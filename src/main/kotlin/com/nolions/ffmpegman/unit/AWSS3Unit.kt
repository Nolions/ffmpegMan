package com.nolions.ffmpegman.unit

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.utils.ThreadFactoryBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence


object AWSS3Unit {
    private lateinit var s3Client: S3Client
    private lateinit var s3AsyncClient: S3AsyncClient
    lateinit var bucket: String
    var poolSize = 20

    fun init(id: String, key: String, bucket: String? = null) {
        val credentials = credentials(id, key)

        this.s3Client = s3Client(credentials)
        this.s3AsyncClient = s3AsyncClient(credentials)

        bucket?.let {
            this.bucket = it
        }
    }

    private fun credentials(id: String, key: String) = AwsBasicCredentials.create(id, key)

    private fun s3Client(credentials: AwsBasicCredentials) = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build()

    private fun s3AsyncClient(credentials: AwsBasicCredentials) = S3AsyncClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .asyncConfiguration { b: ClientAsyncConfiguration.Builder ->
            b.advancedOption(
                SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR,
                poolExecutor(poolSize)
            )
        }
        .build()

    private fun poolExecutor(poolSize: Int? = 10): ThreadPoolExecutor {
        val executor = ThreadPoolExecutor(
            50, 50,
            10, TimeUnit.SECONDS,
            LinkedBlockingQueue(10000),
            ThreadFactoryBuilder()
                .threadNamePrefix("sdk-async-response").build()
        )

        executor.allowCoreThreadTimeOut(true)
        return executor
    }

    /**
     * 上傳一個檔案
     */
    fun putObject(s3Key: String, filePath: String): Boolean {

        return try {
            val objectRequest = PutObjectRequest.builder()
                .bucket(this.bucket)
                .key(s3Key)
                .build()
            s3Client.putObject(objectRequest, RequestBody.fromFile(File(filePath)))
            true
        } catch (e: Exception) {
            println("update AWS fail, err:${e.message}")
            false
        }
    }

    /**
     * 上傳一個檔案
     */
    private fun putObject(s3Key: String, path: Path): CompletableFuture<PutObjectResponse> {
        val request = PutObjectRequest.builder()
            .bucket(this.bucket)
            .key(s3Key)
            .build()

        return s3AsyncClient.putObject(request, path)
    }

    /**
     * 上傳整個資料夾
     */
    @OptIn(ExperimentalPathApi::class)
    fun uploadDirectory(s3Prefix: String, directory: Path) {
        require(directory.isDirectory())

        Files.walk(directory).use { stream ->
            stream.asSequence()
                .filter { it.isRegularFile() }
                .map { path ->
                    putObject("$s3Prefix/${directory.relativize(path)}", path)
                }
                .toList().toTypedArray()
        }.let { CompletableFuture.allOf(*it) }.join()
    }
}