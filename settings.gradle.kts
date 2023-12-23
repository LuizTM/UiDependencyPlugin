pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("examplelibs") {
            from(files("gradle/examplelibs.versions.toml"))
        }
    }
}

rootProject.name = "ui-dependency-plugin"

include(":example")
includeBuild("plugin-build")
