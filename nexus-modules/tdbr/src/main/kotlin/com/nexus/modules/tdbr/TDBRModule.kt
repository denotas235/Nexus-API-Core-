package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline

class TDBRModule : NexusModule {
    override val id = "tdbr"
    override val requiredCapabilities = setOf("DEFERRED_BASELINE")
    override val optionalCapabilities = setOf(
        "FRAMEBUFFER_FETCH", "PIXEL_LOCAL_STORAGE", "FAST_MSAA",
        "ADVANCED_BLENDING", "HDR_COLOR", "SRGB_CORRECTION",
        "PRIMITIVE_BOUNDING_BOX", "BUFFER_STORAGE", "DEBUG_ROBUSTNESS"
    )

    override fun onInitialize(registry: FeatureRegistry) {
        println("[TDBR] Capacidades:")
        println("[TDBR]   PLS   : ${registry.isAvailable("PIXEL_LOCAL_STORAGE")}")
        println("[TDBR]   Fetch : ${registry.isAvailable("FRAMEBUFFER_FETCH")}")
        println("[TDBR]   MSAA  : ${registry.isAvailable("FAST_MSAA")}")
        println("[TDBR]   HDR   : ${registry.isAvailable("HDR_COLOR")}")

        when {
            registry.isAvailable("PIXEL_LOCAL_STORAGE") -> {
                println("[TDBR] Path: Pixel Local Storage")
                PLSManager.setup(1920, 1080)
            }
            registry.isAvailable("FRAMEBUFFER_FETCH") -> {
                println("[TDBR] Path: Framebuffer Fetch")
            }
            else -> {
                println("[TDBR] Path: Forward fallback")
            }
        }
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        pipeline.onBeginFrame {
            // Atualizar direção do sol com base no tempo (futura integração)
            // PLSManager.updateLight(...)
        }
        pipeline.onGeometryPass {
            PLSManager.beginGeometryPass()
        }
        pipeline.onLightingPass {
            PLSManager.endGeometryPass()
            PLSManager.beginLightingPass()
        }
        pipeline.onEndFrame {
            // DiscardHandler virá aqui
        }
        println("[TDBR] Pipeline registado")
    }

    override fun onShutdown() {
        println("[TDBR] Shutdown")
    }
}
