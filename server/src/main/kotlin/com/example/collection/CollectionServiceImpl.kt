package com.example.collection

import java.util.*

class CollectionServiceImpl(val collectionRepository: CollectionRepository) : CollectionService {

    override fun save(documentCollection: DocumentCollection): UUID {
        return collectionRepository.save(documentCollection)
    }

    override fun getById(id: UUID): DocumentCollection {
        return collectionRepository.getById(id)
    }
}