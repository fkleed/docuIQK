package com.example.document

import com.example.shared.Validated
import io.ktor.http.ContentType
import java.io.File
import java.util.*

data class DocumentUpload(
    val id: UUID,
    val name: String,
    val status: DocumentProcessingStatus,
    val collectionId: UUID,
    val tags: Set<UUID>,
    val documentData: File
) {
    companion object {
        const val DOCUMENT_FROM_DATA = "document"
        const val COLLECTION_FORM_DATA = "collection"
        const val TAGS_FORM_DATA = "tags"
        val SUPPORTED_CONTENT_TYPES = setOf<ContentType>(
            ContentType.Application.Pdf
        )

        fun validatedDocumentUpload(
            documentName: String?,
            collectionId: UUID?,
            tags: Set<UUID>?,
            documentData: File?
        ): Validated<DocumentUpload> {
            val validationErrors = mutableSetOf<String>()

            if (documentName.isNullOrBlank()) {
                validationErrors.add("document name is null or blank")
            }

            if (collectionId == null) {
                validationErrors.add("collection id is null")
            }

            if (tags == null) {
                validationErrors.add("tags is null")
            }

            if (documentData == null) {
                validationErrors.add("document data is null")
            }

            if (validationErrors.isEmpty()) {
                val documentUpload = DocumentUpload(
                    UUID.randomUUID(),
                    documentName!!,
                    DocumentProcessingStatus.RECEIVED,
                    collectionId!!,
                    tags!!,
                    documentData!!
                )
                return Validated.Valid(documentUpload)
            } else {
                return Validated.Invalid(validationErrors)
            }
        }
    }
}

enum class DocumentProcessingStatus {
    RECEIVED,
    STORED,
    PROCESSING,
    PROCESSED;

    companion object {
        fun byName(name: String) = valueOf(name.uppercase())
    }
}