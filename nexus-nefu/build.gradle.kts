plugins {
    id("fabric-loom") version "1.16-SNAPSHOT"
    id("java")
}

version = property("mod_version").toString()
group = property("maven_group").toString()
base { archivesName = "nexus-nefu" }

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

// Tarefa para compilar o nativo localmente (só funciona no Termux com NDK)
tasks.register("buildNative") {
    doLast {
        val cppDir = file("src/main/cpp")
        val buildDir = file("build/native")
        buildDir.mkdirs()
        exec {
            workingDir = buildDir
            commandLine("cmake", cppDir.absolutePath,
                "-DCMAKE_TOOLCHAIN_FILE=${System.getenv("NDK")}/build/cmake/android.toolchain.cmake",
                "-DANDROID_ABI=arm64-v8a",
                "-DANDROID_PLATFORM=android-21")
        }
        exec {
            workingDir = buildDir
            commandLine("make", "-j${Runtime.getRuntime().availableProcessors()}")
        }
        copy {
            from(buildDir.resolve("libnefu.so"))
            into(file("src/main/resources/natives/arm64-v8a"))
        }
    }
}

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
