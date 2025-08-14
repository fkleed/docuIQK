package com.example.tag

import java.util.*

interface TagService {

    fun save(tag: Tag): UUID

    fun getById(id: UUID): Tag

    fun update(tag: Tag)

    fun deleteById(id: UUID)
}