import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
    mavenLocal()
    mavenCentral()
    flatDir { dirs(file("../libs")) }
}

loom {
    runs { configureEach { ideConfigGenerated(true) } }
}

dependencies {
    modImplementation("com.nexuapicore:nexus-api-core:1.0.0")
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings("net.fabricmc:yarn:${providers.gradleProperty("yarn_mappings").get()}:v2")
    modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")

    // Apenas as classes GLES, sem natives (o Android fornece libGLESv2.so)
    compileOnly("local:lwjgl-opengles:3.3.3")
    compileOnly("local:lwjgl-egl:3.3.3")
}

tasks.processResources {
    filesMatching("fabric.mod.json") { expand("version" to project.version) }
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    manifest { attributes("Implementation-Version" to project.version) }
}
