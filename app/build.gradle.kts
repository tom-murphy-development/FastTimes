import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)
}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}

android {
    namespace = "com.tmdev.fasttimes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tmdev.fasttimes"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                val prefixMap = "-ffile-prefix-map=${rootProject.projectDir.absolutePath}=."
                cppFlags("-Wl,--build-id=none", prefixMap)
                cFlags("-Wl,--build-id=none", prefixMap)
            }
        }
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists() || System.getenv("RELEASE_KEYSTORE_PATH") != null) {
            create("release") {
                if (keystorePropertiesFile.exists()) {
                    val properties = Properties()
                    properties.load(FileInputStream(keystorePropertiesFile))
                    storeFile = rootProject.file(properties.getProperty("storeFile"))
                    storePassword = properties.getProperty("storePassword")
                    keyAlias = properties.getProperty("keyAlias")
                    keyPassword = properties.getProperty("keyPassword")
                } else {
                    storeFile = rootProject.file(System.getenv("RELEASE_KEYSTORE_PATH"))
                    storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                    keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
                }
            }
        }
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("foss") {
            dimension = "distribution"
        }
        create("playstore") {
            dimension = "distribution"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isMinifyEnabled = false
        }
        create("beta") {
            initWith(getByName("release"))
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-BETA"
            signingConfig = signingConfigs.findByName("release")
            matchingFallbacks += listOf("release")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/NOTICE"
        }
    }

    // AAPT2 Hardening for reproducibility
    aaptOptions {
        additionalParameters("--no-version-vectors", "--no-xml-namespaces")
    }
}

composeCompiler {
    includeSourceInformation = false
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll(
            // Map absolute paths to relative
            "-Xfile-prefix-map=${rootProject.projectDir.absolutePath}=.",
            // Strip absolute paths from Kotlin metadata
            "-Xno-source-debug-extension",
            // Ensure consistent JDK API surface
            "-Xjdk-release=21",
            // Force single-threaded backend for deterministic code generation
            "-Xbackend-threads=1",
            // Stabilize metadata generation
            "-Xallow-no-source-files"
        )
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons)
    implementation(libs.konfetti.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.lifecycle.service)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.compose.foundation)
    implementation(libs.kotlinx.serialization.json)
    implementation("androidx.compose.material3:material3:1.5.0-alpha12")
    implementation("androidx.graphics:graphics-shapes:1.1.0")
    implementation("com.materialkolor:material-kolor:5.0.0-alpha05")
    implementation(libs.compose.runtime)

    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.compose.bom.beta))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk)
}
