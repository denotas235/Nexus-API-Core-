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

    // Bibliotecas GLES/EGL/ShaderC/SPIRV-Cross — directas, sem depender da API Core
    val localGroup = "local"
    val localVer = "3.3.3"
    implementation("${localGroup}:lwjgl-opengles:${localVer}")
    implementation("${localGroup}:lwjgl-egl:${localVer}")
    implementation("${localGroup}:lwjgl-shaderc:${localVer}")
    implementation("${localGroup}:lwjgl-spvc:${localVer}")
    implementation("${localGroup}:lwjgl-opengles-natives-linux-arm64:${localVer}")
    implementation("${localGroup}:lwjgl-shaderc-natives-linux-arm64:${localVer}")
    implementation("${localGroup}:lwjgl-spvc-natives-linux-arm64:${localVer}")
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
