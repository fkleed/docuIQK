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
        val collectionRecord = recordByIdOrElseThrowNotFound(id)
        return collectionRecord.toDocumentCollection()
    }

    override fun update(documentCollection: DocumentCollection) {
        val collectionRecord = recordByIdOrElseThrowNotFound(documentCollection.id)
        collectionRecord.name = documentCollection.name
        collectionRecord.description = documentCollection.description
        collectionRecord.update()
    }

    override fun deleteById(id: UUID) {
        val collectionRecord = recordByIdOrElseThrowNotFound(id)
        collectionRecord.delete()
    }

    private fun recordByIdOrElseThrowNotFound(collectionId: UUID?): CollectionRecord {
        check(collectionId != null)

        val collectionRecord = db.fetchOne(COLLECTION, COLLECTION.ID.eq(collectionId))
            ?: (throw NotFoundException ("Collection with id $collectionId does not exist"))
        return collectionRecord
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
