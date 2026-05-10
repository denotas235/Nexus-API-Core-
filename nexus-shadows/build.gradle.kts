plugins {
    id("fabric-loom") version "1.16-SNAPSHOT"
    id("java")
}
version = property("mod_version").toString()
group = property("maven_group").toString()
base { archivesName = "nexus-shadows" }
repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}
dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}
loom {}
tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") { expand(mapOf("version" to project.version)) }
}
tasks.withType<JavaCompile>().configureEach { options.release = 21 }
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}
