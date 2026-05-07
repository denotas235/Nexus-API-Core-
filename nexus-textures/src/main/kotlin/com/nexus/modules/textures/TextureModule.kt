package com.nexus.modules.textures

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

class TextureModule : ClientModInitializer {
    override fun onInitializeClient() {
        loadInternalASTCTextures()
        
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            object : SimpleSynchronousResourceReloadListener {
                override fun reload(manager: ResourceManager) {
                    val assets = manager.findResources("textures") { it.endsWith(".astc") }
                    ASTCTextureRegistry.updateMap(assets)
                }
                override fun getFabricId(): Identifier = Identifier("nexus", "astc_reload")
            }
        )
    }

    private fun loadInternalASTCTextures() {
        val logger = LoggerFactory.getLogger("nexus-textures")
        try {
            val classLoader = javaClass.classLoader
            val manifestStream = classLoader.getResourceAsStream("assets/maliopt/astc_manifest.json")
            if (manifestStream != null) {
                val manifest = String(manifestStream.readBytes())
                val lines = manifest.lines().filter { it.contains("\"file\":") }
                for (line in lines) {
                    val parts = line.split("\"")
                    if (parts.size >= 4) {
                        val astcPath = parts[3]
                        val resourcePath = "assets/maliopt/$astcPath"
                        val input = classLoader.getResourceAsStream(resourcePath)
                        if (input != null) {
                            val bytes = input.readBytes()
                            val id = Identifier("maliopt", astcPath.removeSuffix(".astc"))
                            ASTCTextureRegistry.put(id, bytes)
                        }
                    }
                }
                logger.info("[TextureModule] {} texturas ASTC internas carregadas", ASTCTextureRegistry.count())
            }
        } catch (e: Exception) {
            logger.warn("[TextureModule] Erro ao carregar texturas ASTC internas: {}", e.message)
        }
    }
}
