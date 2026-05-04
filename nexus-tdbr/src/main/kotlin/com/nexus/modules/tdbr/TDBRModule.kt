package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline
import com.nexus.modules.tdbr.log.TDBRLogger
import com.nexus.modules.tdbr.shader.ShaderExecutionLayer
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
        ShaderExecutionLayer.init()
        val mc = MinecraftClient.getInstance()
        val w = mc?.window?.framebufferWidth ?: 1280
        val h = mc?.window?.framebufferHeight ?: 720
        if (ShaderExecutionLayer.plsAvailable) {
            TDBRLogger.log("Path: Pixel Local Storage (on-chip)")
            PLSManager.setup(w, h)
        } else {
            TDBRLogger.log("Path: MRT Fallback")
            MRTManager.setup(w, h)
        }
        companionModule = this
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        pipeline.onEndFrame {
            if (!PLSManager.ready) return@onEndFrame
            val mc = MinecraftClient.getInstance()
            val fb = mc.framebuffer
            PLSManager.render(fb.colorAttachment, fb.fbo, fb.textureWidth, fb.textureHeight)
            DiscardHandler.discardAfterLighting()
        }
        TDBRLogger.log("Pipeline registado")
    }

    override fun onShutdown() { TDBRLogger.log("Shutdown") }

    companion object {
        var companionModule: TDBRModule? = null
        @JvmStatic fun getModuleInstance(): TDBRModule? = companionModule
    }
}
