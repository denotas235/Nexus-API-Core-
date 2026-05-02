package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline
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
        PLSExtension.init(registry)
        DiscardHandler.init()

        println("[TDBR] Capacidades:")
        println("[TDBR]   PLS   : ${registry.isAvailable("PIXEL_LOCAL_STORAGE")}")
        println("[TDBR]   Fetch : ${registry.isAvailable("FRAMEBUFFER_FETCH")}")
        println("[TDBR]   MSAA  : ${registry.isAvailable("FAST_MSAA")}")
        println("[TDBR]   HDR   : ${registry.isAvailable("COLOR_BUFFER_FLOAT")}")

        val mc = MinecraftClient.getInstance()
        val w = mc?.window?.framebufferWidth  ?: 1280
        val h = mc?.window?.framebufferHeight ?: 720
        println("[TDBR] Resolução detectada: ${w}x${h}")

        when {
            registry.isAvailable("PIXEL_LOCAL_STORAGE") -> {
                println("[TDBR] Path: Pixel Local Storage")
                PLSManager.setup(w, h)
            }
            else -> {
                println("[TDBR] Path: MRT Fallback")
                MRTManager.setup(w, h)
            }
        }
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        pipeline.onBeginFrame { }
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
        println("[TDBR] Pipeline registado")
    }

    override fun onShutdown() {
        println("[TDBR] Shutdown")
    }

    companion object {
        @JvmStatic
        fun onRenderStart() { }
    }
}
