buildscript {
    dependencies {
        classpath(libs.liquibase.core)
    }
}

plugins {
    id(BuildConstants.KOTLIN_CONVENTIONS_PLUGIN)
    alias(libs.plugins.docker.run)
    alias(libs.plugins.liquibase.plugin)
}

dependencies {
    liquibaseRuntime(libs.bundles.liquibase)
}

val dbPortLocal: Int = (project.findProperty("db.port.local") as String).toInt()
val dbNameLocal = "db"
val dbUserLocal = "user"
val dbPasswordLocal = "password"

dockerRun {
    name = "pg-gradle"
    image = "postgres:17.4-alpine3.21"
    ports("$dbPortLocal:5432")
    clean = true
    env(mapOf("POSTGRES_USER" to dbUserLocal, "POSTGRES_PASSWORD" to dbPasswordLocal, "POSTGRES_DB" to dbNameLocal))
    network = "docuiqk-db-network"
}

liquibase {
    val mainChangelog = "src/main/resources/changelog/main.yml"
    val dbUrlLocal = "jdbc:postgresql://localhost:$dbPortLocal/$dbNameLocal?currentSchema=public&user=$dbUserLocal&password=$dbPasswordLocal"
    activities.register("main") {
        this.arguments = mapOf(
            "logLevel" to "info",
            "changelogFile" to mainChangelog,
            "url" to dbUrlLocal,
            "driver" to "org.postgresql.Driver",
        )
    }
    runList = "main"
}