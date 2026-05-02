plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    `maven-publish`
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings("net.fabricmc:yarn:${providers.gradleProperty("yarn_mappings").get()}:v2")
    modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
    
    // Dependência da API Core
    modImplementation("com.nexuapicore:nexus-api-core:1.0.0")
    
    // Alinhamento com a versão da API
    "modClientImplementation"("org.lwjgl:lwjgl-opengl:3.3.6-SNAPSHOT")
}

kotlin { jvmToolchain(21) }
