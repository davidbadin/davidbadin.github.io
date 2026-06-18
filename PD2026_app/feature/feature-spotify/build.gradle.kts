import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { props.load(it) }
}

android {
    namespace = "sk.punkacidetom.pd2026.feature.spotify"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        buildConfigField(
            "String", "SPOTIFY_CLIENT_ID",
            "\"${localProps.getProperty("SPOTIFY_CLIENT_ID", "<PLACEHOLDER_SPOTIFY_CLIENT_ID>")}\"",
        )
        buildConfigField(
            "String", "SPOTIFY_REDIRECT_URI",
            "\"${localProps.getProperty("SPOTIFY_REDIRECT_URI", "pd2026://callback")}\"",
        )
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":core:core-model"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-data"))
    implementation(libs.androidx.core.ktx)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.browser)
    implementation(libs.coil.compose)

    // Spotify Android App Remote SDK — compile-only in this library module.
    // AGP does not allow local .aar as 'implementation' in a library (the classes cannot be
    // re-packaged into the output AAR). Using compileOnly makes the SDK visible to the Kotlin
    // compiler and KSP while keeping it out of the AAR. The app module provides the real
    // (or CI-stub) AAR at link-time via its own 'implementation' declaration.
    // Download the real SDK and place it at:  PD2026_app/libs/spotify-app-remote-release-0.8.0.aar
    val spotifyAar = rootProject.fileTree("libs") { include("spotify-app-remote-release-*.aar") }.firstOrNull()
        ?: file("${rootProject.projectDir}/libs/spotify-app-remote-release-0.8.0.aar")
    compileOnly(files(spotifyAar))
}
