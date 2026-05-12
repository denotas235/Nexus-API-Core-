plugins {
    id("fabric-loom") version "1.6.12"
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
    `maven-publish`
}

repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.0+1.21.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.11+kotlin.2.3.21")
}
