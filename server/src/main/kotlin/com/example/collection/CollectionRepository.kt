package com.example.collection

import java.util.UUID

interface CollectionRepository {

    fun save(documentCollection: DocumentCollection): UUID

    fun findById(id: UUID): DocumentCollection

    fun update(documentCollection: DocumentCollection)

    fun deleteById(id: UUID)
}