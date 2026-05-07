plugins {
    id("fabric-loom")
    kotlin("jvm")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.100.7+1.21.1")
    implementation(project(":nexus-api-core"))
}

loom {
    clientOnlyMinecraftJar()
}
