package com.example.document

import com.example.shared.MinioService
import java.util.UUID

class DocumentServiceImpl(
    val documentRepository: DocumentRepository,
    val minioService: MinioService
) : DocumentService {

    override fun upload(documentUpload: DocumentUpload): UUID {

        val documentId = UUID.randomUUID();

        val minioFileUpload = documentUpload.toMinioFileUpload(documentId.toString())

        minioService.uploadFile(minioFileUpload)

        val document = documentUpload.toDocument(
            documentId,
            DocumentProcessingStatus.UPLOADED
        )
        documentRepository.save(document)

        return documentId
    }

    override fun getById(id: UUID): Document {
        return documentRepository.findById(id)
    }
}