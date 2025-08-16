package com.example.tag

import com.example.docuiqk.data.codegen.jooq.tables.records.TagRecord
import com.example.docuiqk.data.codegen.jooq.tables.references.TAG
import io.ktor.server.plugins.*
import org.jooq.DSLContext
import java.util.*

class JooqTagRepositoryImpl(
    private val db: DSLContext
) : TagRepository {

    override fun save(tag: Tag): UUID {
        val tagId = tag.id ?: UUID.randomUUID()
        db.insertInto(TAG)
            .set(tag.toRecord(tagId))
            .execute()

        return tagId
    }

    override fun findById(id: UUID): Tag {
        val tagRecord = recordByIdOrElseThrowNotFound(id)
        return tagRecord.toTag()
    }

    override fun update(tag: Tag) {
        val tagRecord = recordByIdOrElseThrowNotFound(tag.id)
        tagRecord.name = tag.name
        tagRecord.update()
    }

    override fun deleteById(id: UUID) {
        val tagRecord = recordByIdOrElseThrowNotFound(id)
        tagRecord.delete()
    }

    private fun recordByIdOrElseThrowNotFound(tagId: UUID?): TagRecord {
        check(tagId != null)

        val tagRecord = db.fetchOne(TAG, TAG.ID.eq(tagId))
            ?: (throw NotFoundException ("Tag with id $tagId does not exist"))
        return tagRecord
    }

    private fun Tag.toRecord(tagId: UUID) = TagRecord(
        tagId,
        name
    )

    private fun TagRecord.toTag() = Tag(
        id,
        name
    )
}