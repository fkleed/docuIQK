package com.example.document

import java.util.UUID

class DocumentServiceImpl(val documentRepository: DocumentRepository) : DocumentService {

    override fun upload(document: Document): UUID {
        val documentId = documentRepository.save(document)

        // TODO launch coroutines to upload data to s3 and send notification to RabbitMq

        return documentId
    }

    override fun getById(id: UUID): Document {
        return documentRepository.findById(id)
    }
}