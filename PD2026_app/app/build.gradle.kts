import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

// Load local.properties (SDK path + secrets)
val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { props.load(it) }
}

// Load keystore.properties for release signing (optional — only needed for Play Store)
val keystoreProps = Properties().also { props ->
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { props.load(it) }
}

android {
    namespace = "sk.punkacidetom.pd2026"
    compileSdk = 35

    defaultConfig {
        applicationId = "sk.punkacidetom.pd2026"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose sheet config via BuildConfig
        buildConfigField("String", "SHEET_ID",
            "\"${localProps.getProperty("SHEET_ID", "1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24")}\"")
        buildConfigField("String", "SHEET_GID",
            "\"${localProps.getProperty("SHEET_GID", "968980279")}\"")

        // Spotify
        buildConfigField("String", "SPOTIFY_CLIENT_ID",
            "\"${localProps.getProperty("SPOTIFY_CLIENT_ID", "<PLACEHOLDER_SPOTIFY_CLIENT_ID>")}\"")
        buildConfigField("String", "SPOTIFY_REDIRECT_URI",
            "\"${localProps.getProperty("SPOTIFY_REDIRECT_URI", "pd2026://callback")}\"")
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile", ""))
                storePassword = keystoreProps.getProperty("storePassword", "")
                keyAlias = keystoreProps.getProperty("keyAlias", "")
                keyPassword = keystoreProps.getProperty("keyPassword", "")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystoreProps.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Core modules
    implementation(project(":core:core-model"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-i18n"))
    implementation(project(":core:core-notifications"))

    // Feature modules
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-timetable"))
    implementation(project(":feature:feature-bands"))
    implementation(project(":feature:feature-news"))
    implementation(project(":feature:feature-info"))
    implementation(project(":feature:feature-tickets"))
    implementation(project(":feature:feature-settings"))
    implementation(project(":feature:feature-spotify"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Spotify Android App Remote SDK — runtime classes for feature-spotify (which uses compileOnly)
    // On CI a stub is generated by scripts/create-spotify-stub.py before the build.
    // Developers: download the real SDK and place it at libs/spotify-app-remote-release-0.8.0.aar
    val spotifyAar = rootProject.fileTree("libs") { include("spotify-app-remote-release-*.aar") }.firstOrNull()
        ?: rootProject.file("libs/spotify-app-remote-release-0.8.0.aar")
    implementation(files(spotifyAar))

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
