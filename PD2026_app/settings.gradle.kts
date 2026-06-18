pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PD2026"

// Redirect build dirs outside OneDrive/Dropbox to avoid file-locking errors (Windows dev only)
if (System.getProperty("os.name")?.lowercase()?.contains("windows") == true) {
    gradle.allprojects {
        val relativePath = project.path.replace(":", java.io.File.separator).trimStart(java.io.File.separatorChar)
        layout.buildDirectory.set(File("C:\\Users\\FS0605\\AndroidBuild\\PD2026\\${if (relativePath.isEmpty()) "root" else relativePath}\\build"))
    }
}

include(":app")
include(":core:core-model")
include(":core:core-ui")
include(":core:core-data")
include(":core:core-i18n")
include(":core:core-notifications")
include(":feature:feature-home")
include(":feature:feature-timetable")
include(":feature:feature-bands")
include(":feature:feature-news")
include(":feature:feature-info")
include(":feature:feature-tickets")
include(":feature:feature-settings")
include(":feature:feature-spotify")
