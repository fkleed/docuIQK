package com.example.shared

import io.ktor.server.config.*
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.MinioException
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream

private val LOGGER = LoggerFactory.getLogger("com.example.shared.MinioConfigKt")

val minioKoinModule = module {
    single {
        val config = get<ApplicationConfig>()
        val minioConfig = config.config("minio")
        MinioConfig(
            url = minioConfig.property("url").getString(),
            user = minioConfig.property("user").getString(),
            password = minioConfig.property("password").getString(),
            bucket = minioConfig.property("bucket").getString()
        )
    }

    single { MinioService(get()) }
}

class MinioService(private val minioConfig: MinioConfig) {

    private val minioClient: MinioClient by lazy {
        val client = MinioClient.builder()
            .endpoint(minioConfig.url)
            .credentials(minioConfig.user, minioConfig.password)
            .build()

        if (!client.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.bucket).build())
        }

        client
    }

    fun uploadFile(minioFileUpload: MinioFileUpload) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.bucket)
                    .contentType(minioFileUpload.contentType)
                    .`object`(minioFileUpload.fileName)
                    .stream(
                        ByteArrayInputStream(minioFileUpload.data),
                        minioFileUpload.data.size.toLong(), -1
                    )
                    .build()
            )
        } catch (e: MinioException) {
            LOGGER.error(
                "Minio file upload failed. Filename: {}, Error: {}",
                minioFileUpload.fileName,
                e.message
            )
        }
    }
}

data class MinioConfig(
    val url: String,
    val user: String,
    val password: String,
    val bucket: String
)

data class MinioFileUpload(
    val fileName: String,
    val contentType: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinioFileUpload

        if (fileName != other.fileName) return false
        if (contentType != other.contentType) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

private class MinioConstants {
    companion object {
        const val UPLOAD_PATH = "/upload"
    }
}