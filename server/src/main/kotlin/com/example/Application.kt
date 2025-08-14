package com.example

import com.example.collection.collectionModule
import com.example.file.fileRoutes
import com.example.tag.tagModule
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    collectionModule()
    tagModule()

    routing {
        fileRoutes()
    }

    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when(cause) {
                is IllegalArgumentException -> {
                    call.respondText(text = cause.toString(), status = HttpStatusCode.BadRequest)
                }
                is NotFoundException -> {
                    call.respondText(text = cause.toString(), status = HttpStatusCode.NotFound)
                }
                else -> call.response.status(HttpStatusCode.InternalServerError)
            }
        }
    }

}