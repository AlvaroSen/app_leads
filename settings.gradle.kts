pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.2.2" // Usa la versión que tengas en tu Android Studio
        id("org.jetbrains.kotlin.android") version "1.9.22" // Solo si usas Kotlin
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // necesario para MPAndroidChart
    }
}

rootProject.name = "app_leads"
include(":app")
