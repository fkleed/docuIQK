package com.example.document

import java.util.UUID

class DocumentServiceImpl(val documentRepository: DocumentRepository) : DocumentService {

    override fun upload(document: Document): UUID {
        return documentRepository.save(document)
    }

    override fun getById(id: UUID): Document {
        return documentRepository.findById(id)
    }
}