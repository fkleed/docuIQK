import org.jooq.meta.kotlin.*

buildscript {
    dependencies {
        classpath(libs.liquibase.core)
        classpath(libs.postgresql)
    }
}

plugins {
    id(BuildConstants.KOTLIN_CONVENTIONS_PLUGIN)
    alias(libs.plugins.docker.run)
    alias(libs.plugins.liquibase.plugin)
    alias(libs.plugins.jooq.code.generator)
}

dependencies {
    liquibaseRuntime(libs.bundles.liquibase)
    jooqGenerator(libs.postgresql)
    api(libs.jooq)
    api(libs.postgresql)
}

val dbPortLocal: Int = (requireNotNull(project.findProperty("db.port.local")) as String).toInt()
val pgDbLocal = "postgres"
val pgUserLocal = "postgres"
val pgPasswordLocal = "password"
val dbUrlLocal = "jdbc:postgresql://localhost:$dbPortLocal/$pgDbLocal"

val dbUpdateProd: Boolean = (project.findProperty("db.update.prod") as String?)?.toBoolean() ?: false

dockerRun {
    name = "pg-gradle"
    image = "postgres:17.4-alpine3.21"
    ports("$dbPortLocal:5432")
    clean = true
    env(mapOf("POSTGRES_PASSWORD" to pgPasswordLocal))
}

liquibase {
    activities.register("local") {
        arguments = mapOf(
            "changelogFile" to "src/main/resources/changelog/main.yml",
            "url" to dbUrlLocal,
            "username" to pgUserLocal,
            "password" to pgPasswordLocal,
            "driver" to "org.postgresql.Driver",
            "logLevel" to "info"
        )
    }

    if (dbUpdateProd) {
        val dbHostProd: String = requireNotNull(project.findProperty("db.host.prod")) as String
        val dbPortProd: Int = (requireNotNull(project.findProperty("db.port.prod")) as String).toInt()
        val dbNameProd: String = requireNotNull(project.findProperty("db.name.prod")) as String
        val dbUserProd: String = requireNotNull(project.findProperty("db.user.prod")) as String
        val dbPasswordProd: String = requireNotNull(project.findProperty("db.password.prod")) as String
        val dbUrlProd = "$dbHostProd:$dbPortProd/$dbNameProd"

        activities.register("prod") {
            arguments = mapOf(
                "changelogFile" to "src/main/resources/changelog/main.yml",
                "url" to dbUrlProd,
                "username" to dbUserProd,
                "password" to dbPasswordProd,
                "driver" to "org.postgresql.Driver",
                "logLevel" to "info"
            )
        }
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
}.get()

if (dbUpdateProd) {
    tasks.register("updateProd") {
        group = "liquibase"
        description = "Runs Liquibase update with 'prod' runList"

        doFirst {
            project.liquibase.runList = "prod"
        }

        finalizedBy(liquibaseUpdate)
    }.get()
}

jooq {
    configurations {
        create("main") {

            jooqConfiguration {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc {
                    driver = "org.postgresql.Driver"
                    url = dbUrlLocal
                    user = pgUserLocal
                    password = pgPasswordLocal
                }
                generator {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "databasechangelog|databasechangeloglock"
                    }
                    generate {
                        isPojosAsKotlinDataClasses = true
                        isKotlinNotNullRecordAttributes = true
                    }
                    target {
                        packageName = "${BuildConstants.BASE_PACKAGE_NAME}.data.codegen.jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

sourceSets {
    main {
        kotlin {
            srcDirs(jooq.configurations["main"].outputDir.get().asFile)
        }
    }
}

val dockerStop = tasks.getByName("dockerStop")
val dockerRunTask = tasks.getByName("dockerRun")
val dockerRunStatus = tasks.getByName("dockerRunStatus")

dockerRunTask.dependsOn(dockerStop)
dockerRunTask.doLast {
    Thread.sleep(10000)
}
dockerRunStatus.dependsOn(dockerRunTask)

liquibaseUpdateLocal.dependsOn(dockerRunStatus)

if (dbUpdateProd) {
    val liquibaseUpdateProd = tasks.getByName("updateProd")
    liquibaseUpdateLocal.finalizedBy(liquibaseUpdateProd)
}

val generateJooq = tasks.getByName("generateJooq")
generateJooq.dependsOn(liquibaseUpdateLocal)

publishing {
    publications {
        create<MavenPublication>("data") {
            groupId = BuildConstants.GROUP
            artifactId = "data"
            version = "1.0"
            from(components["kotlin"])
        }
    }
}