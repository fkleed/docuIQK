package com.example.collection

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jooq.DSLContext
import java.util.UUID

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
                val id = collectionService.save(documentCollection)
                call.respond(HttpStatusCode.Created, id.toString())
            }

            get("/{id}") {
                val collectionId = UUID.fromString(call.parameters["id"])
                val collection =  collectionService.getById(collectionId)
                call.respond(collection)
            }
        }
    }
}