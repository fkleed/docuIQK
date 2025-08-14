package com.example.tag

import java.util.UUID

class TagServiceImpl(val tagRepository: TagRepository) : TagService {
    override fun save(tag: Tag): UUID {
        return tagRepository.save(tag)
    }

    override fun getById(id: UUID): Tag {
        return tagRepository.getById(id)
    }

    override fun update(tag: Tag) {
        require(tag.id != null) { "Tag must have an id for update" }
        return tagRepository.update(tag)
    }

    override fun deleteById(id: UUID) {
        return tagRepository.deleteById(id)
    }
}