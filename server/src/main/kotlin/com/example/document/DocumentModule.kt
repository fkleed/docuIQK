package com.example.document

import com.example.tag.Tag
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger("com.example.collection.DocumentModuleKt")

val documentKoinModule = module {
    single { JooqDocumentRepositoryImpl(get()) as DocumentRepository }
    single { DocumentServiceImpl(get()) as DocumentService }
}

fun Application.documentModule() {

    val documentService by inject<DocumentService>()

    routing {
        route("/document") {
            post("/upload") {
                var documentName: String? = null
                var collectionId: UUID? = null
                val tags: MutableSet<Tag> = mutableSetOf()
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
                                    tags.addAll(Json.decodeFromString<Set<Tag>>(part.value))
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

                documentService.upload(document)

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

            get("/collection/{id}") {
                val collectionId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to get all documents for collection with id {}", collectionId)
                call.respond(HttpStatusCode.NotImplemented)
            }

            put {
                val document = call.receive<Document>()
                LOGGER.debug("Request to update document {}", document)
                call.respond(HttpStatusCode.NotImplemented)
            }

            delete("/{id}") {
                val documentId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to delete document with id {}", documentId)
                call.respond(HttpStatusCode.NotImplemented)
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
