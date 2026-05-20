// Top-level build file
plugins {
    // AGP 8.7.x required for compileSdk 35 (Android 15). Gradle wrapper 8.9 (already configured) supports it.
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.24" apply false
}
