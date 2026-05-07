package com.nexus.modules.textures

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import java.io.InputStream
import org.slf4j.LoggerFactory

class TextureModule : ClientModInitializer {
    override fun onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            SimpleSynchronousResourceReloadListener { manager -> scanASTCTextures(manager) }
        )
    }

    private fun scanASTCTextures(manager: ResourceManager) {
        val assets = manager.findResources("textures") { it.endsWith(".astc") }
        ASTCTextureRegistry.updateMap(assets)
    }
}
