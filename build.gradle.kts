import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
    flatDir { dirs("libs") }
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("nexus-api-core") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
}

fabricApi {
    configureDataGeneration { client = true }
}

dependencies {
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings("net.fabricmc:yarn:${providers.gradleProperty("yarn_mappings").get()}:v2")
    modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
    implementation("com.google.code.gson:gson:2.10.1")

    // Bibliotecas GLES/EGL/ShaderC/SPIRV-Cross extraídas do ESCraft
    val localGroup = "local"
    val localLibVersion = "3.3.3"

    listOf("lwjgl-opengles", "lwjgl-egl", "lwjgl-shaderc", "lwjgl-spvc").forEach { artifact ->
        implementation("${localGroup}:${artifact}:${localLibVersion}")
        include("${localGroup}:${artifact}:${localLibVersion}")
    }

    listOf(
        "lwjgl-opengles-natives-linux-arm64",
        "lwjgl-opengles-natives-linux",
        "lwjgl-opengles-natives-windows",
        "lwjgl-shaderc-natives-linux-arm64",
        "lwjgl-shaderc-natives-linux",
        "lwjgl-shaderc-natives-windows",
        "lwjgl-spvc-natives-linux-arm64",
        "lwjgl-spvc-natives-linux",
        "lwjgl-spvc-natives-windows"
    ).forEach { artifact ->
        include("${localGroup}:${artifact}:${localLibVersion}")
    }
}

tasks.processResources {
    val version = version
    inputs.property("version", version)
    filesMatching("fabric.mod.json") { expand("version" to version) }
}

tasks.withType<JavaCompile>().configureEach { options.release = 21 }

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    val projectName = project.name
    inputs.property("projectName", projectName)
    from("LICENSE") { rename { "${it}_$projectName" } }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") { from(components["java"]) }
    }
    repositories { }
}
