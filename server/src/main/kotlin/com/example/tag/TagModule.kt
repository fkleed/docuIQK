package com.example.tag

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger("com.example.tag.TagModuleKt")

fun Application.tagModule() {

    val dslContext: DSLContext by dependencies
    val tagRepository: TagRepository by dependencies

    dependencies {
        provide<TagService> { TagServiceImpl(tagRepository) }
        provide<TagRepository> { JooqTagRepositoryImpl(dslContext) }
    }

    routing {
        route("/tag") {
            val tagService: TagService by dependencies

            post {
                val tag = call.receive<Tag>()
                LOGGER.debug("Request to store tag {}", tag)
                val id = tagService.save(tag)
                call.respond(HttpStatusCode.Created, id.toString())
            }

            get {
                LOGGER.debug("Request to get all tags")
                call.respond(HttpStatusCode.NotImplemented)
            }

            get("/{id}") {
                val tagId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to get tag with id {}", tagId)
                val tag =  tagService.getById(tagId)
                call.respond(tag)
            }

            put {
                val tag = call.receive<Tag>()
                LOGGER.debug("Request to update tag {}", tag)
                tagService.update(tag)
                call.respond(HttpStatusCode.NoContent)
            }

            delete("/{id}") {
                val tagId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to delete tag with id {}", tagId)
                tagService.deleteById(tagId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }

}
