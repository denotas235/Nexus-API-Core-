package com.nexus.modules.textures

import org.slf4j.LoggerFactory

object TextureModule {
    private val logger = LoggerFactory.getLogger("nexus-textures")

    fun load() {
        try {
            val classLoader = TextureModule::class.java.classLoader
            val manifestStream = classLoader.getResourceAsStream("assets/maliopt/astc_manifest.json")
            if (manifestStream != null) {
                val manifest = String(manifestStream.readBytes())
                val lines = manifest.lines().filter { it.contains("\"file\":") }
                for (line in lines) {
                    val parts = line.split("\"")
                    if (parts.size >= 4) {
                        val astcPath = parts[3]  // ex: "textures_astc/block/stone.astc"
                        val resourcePath = "assets/maliopt/$astcPath"
                        val input = classLoader.getResourceAsStream(resourcePath)
                        if (input != null) {
                            val bytes = input.readBytes()
                            // A chave é o caminho relativo sem extensão
                            val key = astcPath.removeSuffix(".astc")
                            ASTCTextureRegistry.put(key, bytes)
                        }
                    }
                }
                logger.info("[TextureModule] {} texturas ASTC carregadas do mod", ASTCTextureRegistry.count())
            }
        } catch (e: Exception) {
            logger.warn("[TextureModule] Erro ao carregar texturas ASTC: {}", e.message)
        }
    }
}
