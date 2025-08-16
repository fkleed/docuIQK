package com.example.document

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger("com.example.collection.DocumentModuleKt")

fun Application.documentModule() {

    val dslContext: DSLContext by dependencies
    val documentRepository: DocumentRepository by dependencies

    dependencies {
        provide<DocumentService> { DocumentServiceImpl(documentRepository) }
        provide<DocumentRepository> { JooqDocumentRepositoryImpl(dslContext) }
    }

    routing {
        route("/document") {
            val documentService: DocumentService by dependencies

            post("/upload") {
                var documentName: String? = null
                var collectionId: UUID? = null
                var tags: MutableSet<UUID> = mutableSetOf()
                var documentData: ByteArray? = null

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
                            documentData = part.provider().readBuffer().readByteArray()
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

                LOGGER.debug(
                    "Request to upload new document. Document name: {} collection id: {}, tags: {}",
                    documentName,
                    collectionId,
                    tags
                )

                val validDocumentUpload = DocumentUpload.validatedDocumentUpload(
                    documentName,
                    collectionId,
                    tags,
                    documentData
                ).getOrThrowIfInvalid()

                val document = validDocumentUpload.toDocument(
                    UUID.randomUUID(),
                    DocumentProcessingStatus.RECEIVED
                )

                documentService.save(document)

                call.respondText(
                    text = document.id.toString(),
                    status = HttpStatusCode.Created
                )
            }

            get("/{id}") {
                val documentId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to get document with id {}", documentId)
                val document = documentService.getById(documentId)
                call.respond(document)
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
