package com.example.file

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import java.util.*

private val LOGGER = KtorSimpleLogger("com.example.FileRoutes")

fun Application.fileRoutes() {
    routing {
        route("/file") {
            post("/upload") {
                val multipart = call.receiveMultipart()

                var fileName: String? = null;
                var fileBytes: ByteArray? = null;
                val tags: MutableSet<String> = mutableSetOf<String>();

                multipart.forEachPart { partData ->
                    when (partData) {
                        is PartData.FileItem -> {
                            if (partData.name == FileRoutesConstants.FILE_FROM_DATA) {

                                if (partData.contentType != ContentType.Application.Pdf) {
                                    throw IllegalArgumentException(FileRoutesConstants.INVALID_FORM_DATA_CONTENT_TYPE)
                                }

                                fileName = partData.originalFileName
                                fileBytes = partData.provider.invoke().readBuffer().readByteArray()

                            } else {
                                throw IllegalArgumentException("${FileRoutesConstants.INVALID_FORM_DATA}: ${partData.name}")
                            }
                        }

                        is PartData.FormItem -> {
                            if (partData.name == FileRoutesConstants.TAGS_FORM_DATA) {
                                tags.add(partData.value)
                            } else {
                                throw IllegalArgumentException("${FileRoutesConstants.INVALID_FORM_DATA}: ${partData.name}")
                            }
                        }

                        else -> throw IllegalArgumentException("${FileRoutesConstants.INVALID_FORM_DATA}: ${partData.name}")
                    }
                }

                val fileUploadId = UUID.randomUUID()

                // TODO save file upload in s3 and db and hand over to file processing service
                val fileUpload = validFileUpload(fileUploadId, fileName, fileBytes, tags)
                LOGGER.debug("Created new file for processing {}", fileUploadId)

                call.respondText(text = fileUploadId.toString(), status =  HttpStatusCode.Created)
            }
        }
    }
}

private fun validFileUpload(fileUploadId: UUID, fileName: String?, fileBytes: ByteArray?, tags: Set<String>): FileUpload {
    val errorPrefix = FileRoutesConstants.FILE_UPLOAD_VALIDATION_FAILED

    require(!fileName.isNullOrBlank()) { "$errorPrefix: fileName is null, empty, or blank" }

    require(fileBytes != null && !fileBytes.isEmpty()) { "$errorPrefix: fileBytes is null or empty" }

    require(tags.isNotEmpty()) { "$errorPrefix: tags are empty" }

    require(tags.none { it.isBlank() }) { "$errorPrefix: one or more tags are blank or empty" }

    return FileUpload(fileUploadId, fileName, fileBytes, tags)
}


private class FileRoutesConstants {
    companion object {
        const val FILE_FROM_DATA = "file"
        const val TAGS_FORM_DATA = "tags"
        const val INVALID_FORM_DATA = "Invalid form data"
        const val INVALID_FORM_DATA_CONTENT_TYPE = "Invalid form data content type provided"
        const val FILE_UPLOAD_VALIDATION_FAILED = "File Upload validation failed"
    }
}



