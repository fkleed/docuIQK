plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.2.0")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}