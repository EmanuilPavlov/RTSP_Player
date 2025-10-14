pluginManagement {
    repositories {
        // Google's Maven repository - required for AndroidX / Media3
        google()
        // Central Maven repository
        mavenCentral()
        // Gradle plugin portal
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Prevent modules from adding their own repositories
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Google's repo is REQUIRED for Media3 and Android dependencies
        google()
        // Maven Central for 3rd-party libraries
        mavenCentral()
    }
}

// Define your project name and modules
rootProject.name = "RTSP_Player"
include(":app")
