package com.example

import com.example.file.fileRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

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
                else -> call.response.status(HttpStatusCode.InternalServerError)
            }
        }
    }

}