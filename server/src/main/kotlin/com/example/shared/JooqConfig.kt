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
        val postgresDSLCofig = PostgresDSLCofig(
            databaseUrl = config.property("database.url").getString(),
            databaseUser = config.property("database.user").getString(),
            databasePassword = config.property("database.password").getString(),
            executeLogging = config.property("logging.jooq.enabled").getString().toBoolean()
        )
        providePostgresDSLContext(postgresDSLCofig)
    }
}


fun providePostgresDSLContext(postgresDSLCofig: PostgresDSLCofig): DSLContext {
    val postgresDataSource = providePostgresDataSource(
        databaseUrl = postgresDSLCofig.databaseUrl,
        databaseUser = postgresDSLCofig.databaseUser,
        databasePassword = postgresDSLCofig.databasePassword
    )

    val settings = Settings().withExecuteLogging(postgresDSLCofig.executeLogging)
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

data class PostgresDSLCofig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val executeLogging: Boolean
)
