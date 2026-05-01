package com.nexuapicore.client

import com.nexuapicore.core.nativelink.NexusNativeLoader
import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.ResourceManager
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("[Nexus] Starting client initialization...")

        // 1. Carregar biblioteca nativa
        NexusNativeLoader.load()
        if (!NexusNativeLoader.loaded) {
            println("[Nexus] Native library not loaded, aborting GPU features.")
            return
        }

        // 2. Inicializar GLAD/Extensões
        if (!NexusNativeLoader.initASTC()) {
            println("[Nexus] GLAD initialization failed.")
            return
        }

        // 3. Lista temporária de extensões (placeholder até a bridge fornecer a lista real)
        val availableExtensions = listOf(
            "GL_ARM_shader_framebuffer_fetch",
            "GL_EXT_shader_framebuffer_fetch",
            "GL_OES_compressed_ETC1_RGB8_texture",
            "GL_KHR_texture_compression_astc_ldr"
        )
        val resolver = CapabilityResolver(availableExtensions)
        val capMap = resolver.resolve()
        val featureRegistry = FeatureRegistry(capMap)

        // 4. Carregar módulos (por enquanto vazio)
        val modules = ModuleLoader.discoverModules()
        modules.forEach {
            it.onInitialize(featureRegistry)
            it.onRegisterPipeline(RenderPipeline)
        }

        // 5. Inicializar recursos e pipeline
        ResourceManager.init()
        RenderPipeline.assemble(modules)

        println("[Nexus] Client initialized. Active capabilities: ${featureRegistry.getActiveCapabilities()}")
    }
}
