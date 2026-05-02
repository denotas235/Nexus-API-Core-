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

        // Verificar PLS via string (sem dependência LWJGL)
        val plsAvailable = PLSManager.detectPLS()

        when {
            plsAvailable -> {
                TDBRLogger.log("Path: Pixel Local Storage (on-chip)")
                PLSManager.enabled = true
                PLSManager.setup(w, h)
                try {
                    val vertSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_gbuffer.vsh")
                    val fragSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_lighting.fsh")
                    ResourceManager.compilePLSShaders(vertSrc, fragSrc)
                    TDBRLogger.log("PLS shaders compiled successfully")
                } catch (e: Exception) {
                    TDBRLogger.log("Error loading PLS shaders: ${e.message}, fallback to MRT")
                    PLSManager.enabled = false
                    MRTManager.setup(w, h)
                    loadMRTShaders()
                }
            }
            else -> {
                TDBRLogger.log("Path: MRT Fallback")
                MRTManager.setup(w, h)
                loadMRTShaders()
            }
        }
        FullscreenQuad.init()
    }

    private fun loadMRTShaders() {
        try {
            val vertSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/mrt_quad.vsh")
            val gBufferFrag = ShaderLoader.load("assets/nexus-tdbr/shaders/mrt_gbuffer.fsh")
            val lightFrag = ShaderLoader.load("assets/nexus-tdbr/shaders/mrt_lighting.fsh")
            ResourceManager.compileMRTShaders(vertSrc, gBufferFrag, vertSrc, lightFrag)
            TDBRLogger.log("MRT shaders compiled successfully")
        } catch (e: Exception) {
            TDBRLogger.log("Error loading MRT shaders: ${e.message}")
        }
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
