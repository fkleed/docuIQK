package com.example.document

import java.util.UUID

interface DocumentService {

    fun upload(documentUpload: DocumentUpload): UUID

    fun getById(id: UUID): Document
}