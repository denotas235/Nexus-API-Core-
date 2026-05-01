plugins {
    // Não especifiques versão aqui – a raiz do projecto já a define.
    kotlin("jvm")
    id("net.fabricmc.fabric-loom-remap")
}

val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_api_version: String by project
val fabric_kotlin_version: String by project

repositories {
    mavenCentral()
}

dependencies {
    // dependência da API Core
    implementation(project(":"))

    // Minecraft e Fabric (necessários para entrypoint)
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

    // YACL (config) – opcional, mas útil
    modImplementation("com.terraformersmc.modmenu:modmenu:11.0.4")
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
