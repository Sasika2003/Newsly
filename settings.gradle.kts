pluginManagement {
    repositories {
        google() // Ensures Google Maven is used for plugins
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Allows repositories to be set at project level
    repositories {
        google() // Google repository for Android dependencies
        mavenCentral()
    }
}

rootProject.name = "Newsly"
include(":app")
