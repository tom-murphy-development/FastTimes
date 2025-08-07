pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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

// Ensure consistent AGP version across the project
gradle.beforeProject {
    val agpVersion = "8.2.0" // Match this with your version catalog
    extensions.findByName("pluginManagement")?.let { pluginManagement ->
        (pluginManagement as PluginManagementSpec).apply {
            plugins {
                id("com.android.application") version agpVersion
                id("com.android.library") version agpVersion
            }
        }
    }
}

rootProject.name = "FastTimes"
include(":app")
