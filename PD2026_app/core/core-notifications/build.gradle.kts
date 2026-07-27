plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sk.punkacidetom.pd2026.core.notifications"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Internal modules
    implementation(project(":core:core-model"))  // Band, BandNotificationScheduler, Stages
    implementation(project(":core:core-data"))   // BandRepository, UserPreferencesRepository

    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.android)
}
