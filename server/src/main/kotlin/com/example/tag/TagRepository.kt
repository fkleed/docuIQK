package com.example.tag

import java.util.*

interface TagRepository {

    fun save(tag: Tag): UUID

    fun findById(id: UUID): Tag

    fun update(tag: Tag)

    fun deleteById(id: UUID)
}