package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline
import com.nexuapicore.core.ResourceManager
import com.nexus.modules.tdbr.log.TDBRLogger
import com.nexus.modules.tdbr.util.ShaderLoader
import com.nexus.modules.tdbr.util.FullscreenQuad
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

        when {
            registry.isAvailable("PIXEL_LOCAL_STORAGE") -> {
                TDBRLogger.log("Path: Pixel Local Storage (on-chip)")
                PLSManager.setup(w, h)
                // Carregar shaders PLS
                try {
                    val vertSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_gbuffer.vsh")
                    val fragSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_lighting.fsh")
                    ResourceManager.compilePLSShaders(vertSrc, fragSrc)
                    TDBRLogger.log("PLS shaders compiled successfully")
                } catch (e: Exception) {
                    TDBRLogger.log("Error loading shaders: ${e.message}")
                    // fallback: manter enabled mas sem shader
                }
            }
            else -> {
                TDBRLogger.log("Path: MRT Fallback")
                MRTManager.setup(w, h)
                // TODO: carregar shaders MRT
            }
        }

        FullscreenQuad.init() // preparar quad
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        pipeline.onGeometryPass {
            if (PLSManager.enabled)      PLSManager.beginGeometryPass()
            else if (MRTManager.enabled) MRTManager.beginGeometryPass()
        }
        pipeline.onLightingPass {
            if (PLSManager.enabled) {
                PLSManager.endGeometryPass()
                PLSManager.beginLightingPass()
            } else if (MRTManager.enabled) {
                MRTManager.endGeometryPass()
                MRTManager.beginLightingPass()
            }
        }
        pipeline.onEndFrame {
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
