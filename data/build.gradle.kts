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

val dbPortLocal: Int = (requireNotNull(project.findProperty("db.port.local")) as String).toInt()
val dbNameLocal = "db"
val dbUserLocal = "user"
val dbPasswordLocal = "password"

val dbHostProd: String = requireNotNull(project.findProperty("db.host.prod")) as String
val dbPortProd: Int = (requireNotNull(project.findProperty("db.port.prod")) as String).toInt()
val dbNameProd: String = requireNotNull(project.findProperty("db.name.prod")) as String
val dbUserProd: String = requireNotNull(project.findProperty("db.user.prod")) as String
val dbPasswordProd: String = requireNotNull(project.findProperty("db.password.prod")) as String

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
    val dbUrlLocal = "jdbc:postgresql://localhost:$dbPortLocal/$dbNameLocal"
    val dbUrlProd = "$dbHostProd:$dbPortProd/$dbNameProd"
    activities.register("local") {
        this.arguments = mapOf(
            "changelogFile" to mainChangelog,
            "url" to dbUrlLocal,
            "username" to dbUserLocal,
            "password" to dbPasswordLocal,
            "driver" to "org.postgresql.Driver",
            "logLevel" to "info",
        )
    }
    activities.register("prod") {
        this.arguments = mapOf(
            "changelogFile" to mainChangelog,
            "url" to dbUrlProd,
            "username" to dbUserProd,
            "password" to dbPasswordProd,
            "driver" to "org.postgresql.Driver",
            "logLevel" to "info",
        )
    }
}

val liquibaseUpdate = tasks.getByName("update")

val liquibaseUpdateLocal = tasks.register("updateLocal") {
    group = "liquibase"
    description = "Runs Liquibase update with 'local' runList"

    doFirst {
        project.liquibase.runList = "local"
    }

    finalizedBy(liquibaseUpdate)

}

val liquibaseUpdateProd = tasks.register("updateProd") {
    group = "liquibase"
    description = "Runs Liquibase update with 'prod' runList"

    doFirst {
        project.liquibase.runList = "prod"
    }

    finalizedBy(liquibaseUpdate)

}