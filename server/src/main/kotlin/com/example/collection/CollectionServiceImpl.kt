package com.example.collection

import io.ktor.util.logging.*
import java.util.*

private val LOGGER = KtorSimpleLogger("com.example.CollectionServiceImpl")

class CollectionServiceImpl(val collectionRepository: CollectionRepository) : CollectionService {

    override fun save(documentCollection: DocumentCollection): UUID {
        LOGGER.debug("Request to store collection")
        return collectionRepository.save(documentCollection)
    }

    override fun getById(id: UUID): DocumentCollection {
        LOGGER.debug("Request to get collection with id {}", id)
        return collectionRepository.getById(id)
    }
}