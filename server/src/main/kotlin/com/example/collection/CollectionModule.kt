package com.example.collection

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger("com.example.collection.CollectionModuleKt")

val collectionKoinModule = module {
    single { JooqCollectionRepositoryImpl(get()) as CollectionRepository }
    single { CollectionServiceImpl(get()) as CollectionService }
}

fun Application.collectionModule() {

    val collectionService by inject<CollectionService>()

    routing {
        route("/collection") {

            post {
                val documentCollection = call.receive<DocumentCollection>()
                LOGGER.debug("Request to store collection {}", documentCollection)
                val id = collectionService.save(documentCollection)
                call.respond(HttpStatusCode.Created, id.toString())
            }

            get {
                LOGGER.debug("Request to get all collections")
                call.respond(HttpStatusCode.NotImplemented)
            }

            get("/{id}") {
                val collectionId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to get collection with id {}", collectionId)
                val collection =  collectionService.getById(collectionId)
                call.respond(collection)
            }

            put {
                val documentCollection = call.receive<DocumentCollection>()
                LOGGER.debug("Request to update collection {}", documentCollection)
                collectionService.update(documentCollection)
                call.respond(HttpStatusCode.NoContent)
            }
            delete("/{id}") {
                val collectionId = UUID.fromString(call.parameters["id"])
                LOGGER.debug("Request to delete collection with id {}", collectionId)
                collectionService.deleteById(collectionId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}