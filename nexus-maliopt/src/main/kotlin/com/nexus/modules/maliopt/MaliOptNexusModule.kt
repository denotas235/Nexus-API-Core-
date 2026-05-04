package com.nexus.modules.maliopt

import com.maliopt.MaliOptMod
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline

class MaliOptNexusModule : NexusModule {
    override val id = "maliopt"
    override val requiredCapabilities = setOf("DEFERRED_BASELINE")
    override val optionalCapabilities = setOf(
        "PIXEL_LOCAL_STORAGE", "FRAMEBUFFER_FETCH",
        "ASTC_LDR", "ASTC_HDR", "SRGB_CORRECTION"
    )

    override fun onInitialize(registry: FeatureRegistry) {
        // O MaliOpt já tem a sua própria inicialização — mantemos intacta
        println("[Nexus/MaliOpt] Inicializando via Nexus API Core")
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        // O MaliOpt usa WorldRenderEvents.END internamente — não precisa do nosso pipeline
    }

    override fun onShutdown() { }
}
