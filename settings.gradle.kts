include("server", "kooq", "data")

rootProject.name = "docuIQK"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
