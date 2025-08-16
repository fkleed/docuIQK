package com.example.document

import com.example.docuiqk.data.codegen.jooq.tables.records.DocumentRecord
import com.example.docuiqk.data.codegen.jooq.tables.records.DocumentTagRecord
import com.example.docuiqk.data.codegen.jooq.tables.references.DOCUMENT
import com.example.docuiqk.data.codegen.jooq.tables.references.DOCUMENT_TAG
import io.ktor.server.plugins.*
import org.jooq.DSLContext
import java.util.*

class JooqDocumentRepositoryImpl(
    private val db: DSLContext
) : DocumentRepository {

    override fun save(document: Document): UUID {
        db.transaction { trx ->
            trx.dsl().insertInto(DOCUMENT)
                .set(document.toRecord())
                .execute()

            trx.dsl().insertInto(DOCUMENT_TAG)
                .set(document.tagRecords())
                .execute()
        }

        return document.id
    }

    override fun findById(id: UUID): Document {
        val documentRecord = recordByIdOrElseThrowNotFound(id)
        val tags = db.fetch(DOCUMENT_TAG, DOCUMENT_TAG.document.ID.eq(id))
            .getValues(DOCUMENT_TAG.TAG_ID)
            .filterNotNull()
            .toSet()

        return documentRecord.toDocument(tags)
    }

    private fun recordByIdOrElseThrowNotFound(documentId: UUID?): DocumentRecord {
        check(documentId != null)

        val documentRecord = db.fetchOne(DOCUMENT, DOCUMENT.ID.eq(documentId))
            ?: (throw NotFoundException ("Document with id $documentId does not exist"))
        return documentRecord
    }

    private fun Document.toRecord() = DocumentRecord(
        id,
        name,
        status.toString(),
        collectionId
    )

    private fun Document.tagRecords(): List<DocumentTagRecord> =
        tags.map { DocumentTagRecord(id, it) }

    private fun DocumentRecord.toDocument(tags: Set<UUID>) = Document(
        id,
        name,
        DocumentProcessingStatus.byName(status),
        collection,
        tags
    )
}