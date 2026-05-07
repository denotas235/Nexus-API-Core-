pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
    }
}
rootProject.name = "nexus-api-core"
include(":nexus-maliopt")
include(":nexus-audio")
