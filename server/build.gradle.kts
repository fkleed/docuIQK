plugins {
    id(BuildConstants.KOTLIN_CONVENTIONS_PLUGIN)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version libs.versions.serialization.plugin
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-di")

    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.data)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}