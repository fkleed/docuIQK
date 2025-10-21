package com.example.collection

import java.util.*

class CollectionServiceImpl(val collectionRepository: CollectionRepository) : CollectionService {

    override fun save(documentCollection: DocumentCollection): UUID {
        return collectionRepository.save(documentCollection)
    }

    override fun getById(id: UUID): DocumentCollection {
        return collectionRepository.findById(id)
    }

    override fun update(documentCollection: DocumentCollection) {
        require(documentCollection.id != null) { "Document collection must have an id for update" }
        return collectionRepository.update(documentCollection)
    }

    override fun deleteById(id: UUID) {
        collectionRepository.deleteById(id)
    }
}