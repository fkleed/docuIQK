package com.example

import com.example.collection.collectionKoinModule
import com.example.collection.collectionModule
import com.example.document.documentKoinModule
import com.example.document.documentModule
import com.example.shared.jooqKoinModule
import com.example.tag.tagKoinModule
import com.example.tag.tagModule
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.main() {
    collectionModule()
    tagModule()
    documentModule()

    install(ContentNegotiation) {
        json()
    }

    install(Koin) {
        slf4jLogger()

        val applicationModule = module {
            single { environment.config }
        }

        modules(
            applicationModule,
            jooqKoinModule,
            documentKoinModule,
            collectionKoinModule,
            tagKoinModule
        )
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