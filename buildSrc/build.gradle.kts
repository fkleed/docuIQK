plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.25")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}