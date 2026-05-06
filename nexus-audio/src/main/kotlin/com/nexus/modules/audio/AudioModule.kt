package com.nexus.modules.audio

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline

class AudioModule : NexusModule {
    override val id = "nexus-audio"
    override val requiredCapabilities = setOf("AUDIO_AAUDIO")
    override val optionalCapabilities = setOf("AUDIO_HRTF", "AUDIO_EFX")

    override fun onInitialize(registry: FeatureRegistry) {
        println("[NexusAudio] Inicializando engine de áudio...")
        AudioEngine.init(registry)
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {}
    override fun onShutdown() { AudioEngine.shutdown() }
}
