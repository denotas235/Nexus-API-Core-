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
        // Inicializa PLSExtension com o registry real — a partir daqui
        // isAvailable() lê GL_EXT_shader_pixel_local_storage via extensions.json
        PLSExtension.init(registry)
        DiscardHandler.init()

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
            else -> {
                println("[TDBR] Path: MRT Fallback")
                MRTManager.setup(1920, 1080)
            }
        }
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        pipeline.onBeginFrame {
            // Futura integração: PLSManager.updateLight(...)
        }
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
}
