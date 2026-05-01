package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline

class TDBRModule : NexusModule {
    override val id = "tdbr"
    override val requiredCapabilities = setOf("DEFERRED_BASELINE")
    override val optionalCapabilities = setOf(
        "FRAMEBUFFER_FETCH",
        "PIXEL_LOCAL_STORAGE",
        "FAST_MSAA",
        "ADVANCED_BLENDING",
        "HDR_COLOR",
        "SRGB_CORRECTION",
        "PRIMITIVE_BOUNDING_BOX",
        "BUFFER_STORAGE",
        "DEBUG_ROBUSTNESS"
    )

    override fun onInitialize(registry: FeatureRegistry) {
        println("[TDBR] Initializing with capabilities:")
        println("[TDBR]   PLS : ${registry.isAvailable("PIXEL_LOCAL_STORAGE")}")
        println("[TDBR]   Fetch : ${registry.isAvailable("FRAMEBUFFER_FETCH")}")
        println("[TDBR]   MSAA : ${registry.isAvailable("FAST_MSAA")}")
        println("[TDBR]   HDR  : ${registry.isAvailable("HDR_COLOR")}")

        // Escolher caminho de renderização
        when {
            registry.isAvailable("PIXEL_LOCAL_STORAGE") -> {
                println("[TDBR] Path: Pixel Local Storage (on-chip G‑buffer)")
                // PLSManager.setup(...)
            }
            registry.isAvailable("FRAMEBUFFER_FETCH") -> {
                println("[TDBR] Path: Framebuffer Fetch (deferred via MRTs + fetch)")
                // MRTFallback.setup(...)
            }
            else -> {
                println("[TDBR] Path: Fallback forward rendering")
            }
        }
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        // Registar callbacks nos estágios do pipeline
        // pipeline.onGeometryPass { ... }
        // pipeline.onLightingPass { ... }
        println("[TDBR] Registered on pipeline")
    }

    override fun onShutdown() {
        println("[TDBR] Shutdown")
    }
}
