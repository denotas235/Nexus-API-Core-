plugins {
    id("fabric-loom") version "1.7.+"
}

version = "1.0.0"
group = "com.nexus.textures"

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.0+1.21.1")
}

loom {
    splitEnvironmentSourceSets()
    mixin {
        add(sourceSets.main.get(), "nexus-textures.refmap.json")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}
