pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val agpVersion = "8.2.0" // Match this with your version catalog
        id("com.android.application") version agpVersion apply false
        id("com.android.library") version agpVersion apply false

    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FastTimes"
include(":app")
