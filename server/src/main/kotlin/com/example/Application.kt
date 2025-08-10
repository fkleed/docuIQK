package com.example

import com.example.collection.collectionModule
import com.example.file.fileRoutes
import com.example.shared.providePostgresDSLContext
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.di.annotations.Property
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import org.jooq.DSLContext
import kotlin.reflect.KClass

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    collectionModule()

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