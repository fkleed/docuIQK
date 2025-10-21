package com.example.document

import com.example.docuiqk.data.codegen.jooq.tables.records.DocumentRecord
import com.example.docuiqk.data.codegen.jooq.tables.records.DocumentTagRecord
import com.example.docuiqk.data.codegen.jooq.tables.references.DOCUMENT
import com.example.docuiqk.data.codegen.jooq.tables.references.DOCUMENT_TAG
import com.example.docuiqk.data.codegen.jooq.tables.references.TAG
import com.example.tag.JooqTagRepositoryImpl.Companion.toTag
import com.example.tag.Tag
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
        val tagRecords = db.select()
            .from(TAG)
            .join(DOCUMENT_TAG)
            .on(TAG.ID.eq(DOCUMENT_TAG.tag.ID))
            .where(DOCUMENT_TAG.document.ID.eq(id))
            .fetch()

        val tags = tagRecords.map{ record -> record.into(TAG) }.map { tagRecord -> tagRecord.toTag() }.toSet()

        return documentRecord.toDocument(tags)
    }

    private fun recordByIdOrElseThrowNotFound(documentId: UUID?): DocumentRecord {
        check(documentId != null)

        val documentRecord = db.fetchOne(DOCUMENT, DOCUMENT.ID.eq(documentId))
            ?: (throw NotFoundException ("Document with id $documentId does not exist"))
        return documentRecord
    }

    companion object {
        private fun Document.toRecord() = DocumentRecord(
            id,
            name,
            status.toString(),
            collectionId
        )

        private fun Document.tagRecords(): List<DocumentTagRecord> =
            tags.filterNot{ it.id == null }.map { DocumentTagRecord(id, it.id!!) }

        private fun DocumentRecord.toDocument(tags: Set<Tag>) = Document(
            id,
            name,
            DocumentProcessingStatus.byName(status),
            collection,
            tags
        )
    }
}