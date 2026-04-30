
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}

tasks.register("installGitHook") {
    group = "verification"
    description = "Installs the Git pre-commit hook."
    doLast {
        copy {
            from(file("scripts/pre-commit"))
            into(file(".git/hooks"))
        }
        val hookFile = file(".git/hooks/pre-commit")
        if (hookFile.exists()) {
            hookFile.setExecutable(true)
            println("Git pre-commit hook installed successfully.")
        } else {
            error("Failed to install Git pre-commit hook: .git/hooks directory not found.")
        }
    }
}

// Automatically install the hook when the project is evaluated or during a build
tasks.named("clean") {
    dependsOn("installGitHook")
}
