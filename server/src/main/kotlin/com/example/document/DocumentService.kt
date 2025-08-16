package com.example.document

import java.util.UUID

interface DocumentService {

    fun save(document: Document): UUID

    fun getById(id: UUID): Document
}