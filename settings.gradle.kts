plugins {
    // Lets Gradle download the JDK in jvmToolchain() if it isn't installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "render-schedule"
