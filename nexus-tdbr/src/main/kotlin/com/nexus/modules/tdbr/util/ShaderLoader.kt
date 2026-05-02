package com.nexus.modules.tdbr.util

import java.io.InputStream

object ShaderLoader {
    fun load(path: String): String {
        val input: InputStream = ShaderLoader::class.java.classLoader.getResourceAsStream(path)
            ?: throw RuntimeException("Shader not found: $path")
        return input.bufferedReader().readText()
    }
}
