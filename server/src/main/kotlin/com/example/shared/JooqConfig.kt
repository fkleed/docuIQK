package com.example.shared

import io.ktor.server.plugins.di.annotations.Property
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

fun providePostgresDSLContext(
    @Property("database.url") databaseUrl: String,
    @Property("database.user") databaseUser: String,
    @Property("database.password") databasePassword: String,
    @Property("logging.jooq.enabled") executeLogging: Boolean
): DSLContext {
    val postgresDataSource = providePostgresDataSource(
        databaseUrl,
        databaseUser,
        databasePassword
    )

    val settings = Settings().withExecuteLogging(executeLogging)
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