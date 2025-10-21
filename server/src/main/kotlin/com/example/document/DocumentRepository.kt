package com.example.document

import java.util.UUID

interface DocumentRepository {

    fun save(document: Document): UUID

    fun findById(id: UUID): Document
}