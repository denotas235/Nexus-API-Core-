package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline
import com.nexus.modules.tdbr.log.TDBRLogger
import com.nexus.modules.tdbr.util.FullscreenQuad
import com.nexus.modules.tdbr.util.ShaderLoader
import net.minecraft.client.MinecraftClient

class TDBRModule : NexusModule {
    override val id = "tdbr"
    override val requiredCapabilities = setOf("DEFERRED_BASELINE")
    override val optionalCapabilities = setOf(
        "FRAMEBUFFER_FETCH", "PIXEL_LOCAL_STORAGE", "FAST_MSAA",
        "ADVANCED_BLENDING", "HDR_COLOR", "SRGB_CORRECTION",
        "PRIMITIVE_BOUNDING_BOX", "BUFFER_STORAGE", "DEBUG_ROBUSTNESS"
    )

    override fun onInitialize(registry: FeatureRegistry) {
        TDBRLogger.log("Initializing TDBR Module")
        PLSExtension.init(registry)
        DiscardHandler.init()

        val mc = MinecraftClient.getInstance()
        val w = mc?.window?.framebufferWidth  ?: 1280
        val h = mc?.window?.framebufferHeight ?: 720

        TDBRLogger.log("Path: Pixel Local Storage (on-chip)")
        PLSManager.setup(w, h)
        FullscreenQuad.init()

        try {
            val vertSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_gbuffer.vsh")
            val fragSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_lighting.fsh")
            ResourceManager.compilePLSShaders(vertSrc, fragSrc)
            TDBRLogger.log("PLS shaders compiled successfully, handle=${ResourceManager.plsShaderHandle}")
        } catch (e: Exception) {
            TDBRLogger.log("Error loading PLS shaders: ${e.message}")
        }
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        pipeline.onEndFrame {
            PLSManager.renderOutput()
            DiscardHandler.discardAfterLighting()
        }
        TDBRLogger.log("Pipeline registado")
    }

    override fun onShutdown() {
        TDBRLogger.log("Shutdown")
    }

    companion object {
        @JvmStatic fun onRenderStart() { }
    }
}
