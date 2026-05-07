package com.nexus.modules.textures

import com.nexus.modules.textures.ASTCTextureRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import java.io.InputStream

class TextureModule : ClientModInitializer {
    override fun onInitializeClient() {
        // Carrega imediatamente as texturas ASTC da pasta interna do mod
        loadInternalASTCTextures()
        
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            SimpleSynchronousResourceReloadListener { manager ->
                scanASTCTextures(manager)
            }
        )
    }

    private fun loadInternalASTCTextures() {
        val logger = LoggerFactory.getLogger("nexus-textures")
        try {
            val classLoader = javaClass.classLoader
            val manifestStream = classLoader.getResourceAsStream("assets/maliopt/astc_manifest.json")
            if (manifestStream != null) {
                // Lê o manifesto e carrega os ficheiros .astc correspondentes
                val manifest = String(manifestStream.readBytes())
                // Percorre as entradas do manifesto
                val lines = manifest.lines().filter { it.contains("\"file\":") }
                for (line in lines) {
                    val parts = line.split("\"")
                    if (parts.size >= 4) {
                        val astcPath = parts[3] // ex: "textures_astc/block/stone.astc"
                        val resourcePath = "assets/maliopt/$astcPath"
                        val input = classLoader.getResourceAsStream(resourcePath)
                        if (input != null) {
                            val bytes = input.readBytes()
                            // Converte o caminho para Identifier
                            val id = Identifier("maliopt", astcPath.removeSuffix(".astc"))
                            ASTCTextureRegistry.put(id, bytes)
                        }
                    }
                }
                logger.info("[TextureModule] ${ASTCTextureRegistry.count()} texturas ASTC internas carregadas")
            }
        } catch (e: Exception) {
            logger.warn("[TextureModule] Erro ao carregar texturas ASTC internas: ${e.message}")
        }
    }

    private fun scanASTCTextures(manager: ResourceManager) {
        val assets = manager.findResources("textures") { it.endsWith(".astc") }
        ASTCTextureRegistry.updateMap(assets)
    }
}
