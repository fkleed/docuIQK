package com.example.collection

import com.example.docuiqk.data.codegen.jooq.tables.records.CollectionRecord
import com.example.docuiqk.data.codegen.jooq.tables.references.COLLECTION
import io.ktor.server.plugins.NotFoundException
import org.jooq.DSLContext
import java.util.UUID

class JooqCollectionRepositoryImpl(
    private val db: DSLContext
) : CollectionRepository {

    override fun save(documentCollection: DocumentCollection): UUID {
        val collectionId = documentCollection.id ?: UUID.randomUUID()

        db.insertInto(COLLECTION)
            .set(documentCollection.toRecord(collectionId))
            .execute()

        return collectionId
    }

    override fun getById(id: UUID): DocumentCollection {
        val documentCollection = db.fetchOne(COLLECTION, COLLECTION.ID.eq(id))?.toDocumentCollection()
        return documentCollection ?: throw NotFoundException("Collection with id $id does not exist")
    }

    private fun DocumentCollection.toRecord(collectionId: UUID) = CollectionRecord(
        collectionId,
        name,
        description
    )

    private fun CollectionRecord.toDocumentCollection() = DocumentCollection(
        id,
        name,
        description
    )
}
