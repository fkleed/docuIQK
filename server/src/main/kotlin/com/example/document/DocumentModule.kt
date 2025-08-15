package com.example.document

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

private val LOGGER = LoggerFactory.getLogger("com.example.collection.DocumentModuleKt")

fun Application.documentModule() {

    routing {
        route("/document") {
            post("/upload") {
                var documentName: String? = null
                var collectionId: UUID? = null
                var tags: MutableSet<UUID> = mutableSetOf()
                var documentData: File? = null

                val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            require(part.name == DocumentUpload.DOCUMENT_FROM_DATA) {
                                DocumentRouteConstants.INVALID_FORM_DATA
                            }
                            require(DocumentUpload.SUPPORTED_CONTENT_TYPES.contains(part.contentType)) {
                                DocumentRouteConstants.INVALID_FORM_DATA_CONTENT_TYPE
                            }

                            documentName = part.originalFileName
                            documentData = File(documentName)
                            part.provider().copyAndClose(documentData.writeChannel())
                        }

                        is PartData.FormItem -> {
                            when (part.name) {
                                DocumentUpload.COLLECTION_FORM_DATA -> {
                                    collectionId = UUID.fromString(part.value)
                                }

                                DocumentUpload.TAGS_FORM_DATA -> {
                                    val json = Json { ignoreUnknownKeys = true }
                                    val tagStrings: List<String> = json.decodeFromString(part.value)
                                    tagStrings.map(UUID::fromString).let {
                                        tags.addAll(it)
                                    }
                                }

                                else -> throw IllegalArgumentException(DocumentRouteConstants.INVALID_FORM_DATA)
                            }
                        }

                        else -> throw IllegalArgumentException(DocumentRouteConstants.INVALID_FORM_DATA)
                    }
                    part.dispose()
                }

                val validDocumentUpload = DocumentUpload.validatedDocumentUpload(
                    documentName,
                    collectionId,
                    tags,
                    documentData
                ).getOrThrowIfInvalid()

                LOGGER.debug("Created new file for processing {}", validDocumentUpload.id)

                call.respondText(
                    text = validDocumentUpload.id.toString(),
                    status = HttpStatusCode.Created
                )
            }
        }
    }
}

private class DocumentRouteConstants {
    companion object {
        const val INVALID_FORM_DATA = "Invalid form data"
        const val INVALID_FORM_DATA_CONTENT_TYPE = "Invalid form data content type provided"
    }
}
