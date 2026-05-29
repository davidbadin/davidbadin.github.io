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
