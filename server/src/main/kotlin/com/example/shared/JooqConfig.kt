package com.example.shared

import io.ktor.server.config.ApplicationConfig
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.koin.dsl.module
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

val jooqKoinModule = module {
    single {
        val config = get<ApplicationConfig>()
        val postgresDSLContext = PostgresDSLContext(
            databaseUrl = config.property("database.url").getString(),
            databaseUser = config.property("database.user").getString(),
            databasePassword = config.property("database.password").getString(),
            executeLogging = config.property("logging.jooq.enabled").getString().toBoolean()
        )
        providePostgresDSLContext(postgresDSLContext)
    }
}


fun providePostgresDSLContext(postgresDSLContext: PostgresDSLContext): DSLContext {
    val postgresDataSource = providePostgresDataSource(
        databaseUrl = postgresDSLContext.databaseUrl,
        databaseUser = postgresDSLContext.databaseUser,
        databasePassword = postgresDSLContext.databasePassword
    )

    val settings = Settings().withExecuteLogging(postgresDSLContext.executeLogging)
    val dslContext = DSL.using(postgresDataSource, SQLDialect.POSTGRES, settings)
    return dslContext
}

private fun providePostgresDataSource(
    databaseUrl: String,
    databaseUser: String,
    databasePassword: String

): DataSource {

    val dataSource = PGSimpleDataSource()
    return dataSource.apply {
        setUrl(databaseUrl)
        user = databaseUser
        password = databasePassword
    }
}

data class PostgresDSLContext(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val executeLogging: Boolean
)
