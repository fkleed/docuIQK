plugins {
    id(BuildConstants.KOTLIN_CONVENTIONS_PLUGIN)
    alias(libs.plugins.ktor)
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-status-pages")

    implementation(libs.logback)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}