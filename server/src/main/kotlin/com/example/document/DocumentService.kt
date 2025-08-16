package com.example.document

import java.util.UUID

interface DocumentService {

    fun upload(document: Document): UUID

    fun getById(id: UUID): Document
}