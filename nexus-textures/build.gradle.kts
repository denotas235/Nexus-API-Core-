plugins {
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
}

version = "1.0.0"
group = "com.nexus"

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")
}

loom {
    mods {
        create("nexus-textures") {
            sourceSet(sourceSets.main.get())
        }
    }
}

