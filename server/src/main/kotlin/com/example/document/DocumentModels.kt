package com.example.document

import com.example.shared.UUIDSerializer
import com.example.shared.Validated
import com.example.tag.Tag
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

data class DocumentUpload(
    val name: String,
    val collectionId: UUID,
    val tags: Set<Tag>,
    val documentData: ByteArray
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
            tags: Set<Tag>,
            documentData: ByteArray?
        ): Validated<DocumentUpload> {
            val validationErrors = mutableSetOf<String>()

            if (documentName.isNullOrBlank()) {
                validationErrors.add("document name is null or blank")
            }

            if (collectionId == null) {
                validationErrors.add("collection id is null")
            }

            validationErrors.addAll(
                tags.filter { it.id == null }
                    .map { "tag ${it.name} has no id" }
            )

            if (documentData == null) {
                validationErrors.add("document data is null")
            } else {
                val pdfHeader = "%PDF-".toByteArray()
                if (documentData.size < pdfHeader.size || !documentData.take(pdfHeader.size).toByteArray()
                        .contentEquals(pdfHeader)
                ) {
                    validationErrors.add("document data is not a valid PDF")
                }
            }

            if (validationErrors.isEmpty()) {
                val documentUpload = DocumentUpload(
                    documentName!!,
                    collectionId!!,
                    tags,
                    documentData!!
                )
                return Validated.Valid(documentUpload)
            } else {
                return Validated.Invalid(validationErrors)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentUpload

        if (name != other.name) return false
        if (collectionId != other.collectionId) return false
        if (tags != other.tags) return false
        if (!documentData.contentEquals(other.documentData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + collectionId.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + documentData.contentHashCode()
        return result
    }
}

@Serializable
data class Document(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    val status: DocumentProcessingStatus,
    @Serializable(with = UUIDSerializer::class) val collectionId: UUID,
    val tags: Set<Tag>
)


fun DocumentUpload.toDocument(documentId: UUID, processingStatus: DocumentProcessingStatus) = Document(
    documentId,
    name,
    processingStatus,
    collectionId,
    tags
)

@Serializable
enum class DocumentProcessingStatus {
    RECEIVED,
    UPLOADED,
    PROCESSING,
    PROCESSED;

    companion object {
        fun byName(name: String) = valueOf(name.uppercase())
    }
}