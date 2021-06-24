rootProject.name = "TrainingAssistant"

pluginManagement {
    val kotlinVersion: String by settings
    val shadowJarVersion: String by settings

    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.github.johnrengelman.shadow") {
                useModule("com.github.jengelman.gradle.plugins:shadow:$shadowJarVersion")
            }
        }
    }
}