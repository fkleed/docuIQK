package com.example.collection

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger("com.example.collection.CollectionModuleKt")

fun Application.collectionModule() {

    val dslContext: DSLContext by dependencies
    val collectionRepository: CollectionRepository by dependencies

    dependencies {
        provide<CollectionService> { CollectionServiceImpl(collectionRepository) }
        provide<CollectionRepository> { JooqCollectionRepositoryImpl(dslContext) }
    }

    routing {
        route("/collection") {
            val collectionService: CollectionService by dependencies

            post {
                val documentCollection = call.receive<DocumentCollection>()
                LOGGER.debug("Request to store collection")
                val id = collectionService.save(documentCollection)
                call.respond(HttpStatusCode.Created, id.toString())
            }

            get("/{id}") {
                val collectionId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to get collection with id {}", collectionId)
                val collection =  collectionService.getById(collectionId)
                call.respond(collection)
            }
        }
    }
}