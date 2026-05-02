import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
}

version = "1.0.0"
group = "com.nexus.modules"

repositories {
    mavenLocal()           // <- API Core publicada aqui
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    // Dependência da API Core (publicada localmente)
    modImplementation("com.nexuapicore:nexus-api-core:1.0.0")

    // Minecraft e Fabric
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.19.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.116.11+1.21.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.11+kotlin.2.3.21")
}

loom {
    // O TDBR é apenas cliente
    runs {
        configureEach {
            ideConfigGenerated(true)
        }
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
