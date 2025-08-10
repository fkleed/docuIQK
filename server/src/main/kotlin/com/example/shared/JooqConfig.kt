package com.example.shared

import io.ktor.server.plugins.di.annotations.Property
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

fun providePostgresDSLContext(
    @Property("database.url") databaseUrl: String,
    @Property("database.user") databaseUser: String,
    @Property("database.password") databasePassword: String
): DSLContext {
    val postgresDataSource = providePostgresDataSource(
        databaseUrl,
        databaseUser,
        databasePassword
    )

    val dslContext = DSL.using(postgresDataSource, SQLDialect.POSTGRES)
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