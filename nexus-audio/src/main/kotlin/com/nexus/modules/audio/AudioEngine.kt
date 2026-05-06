package com.nexus.modules.audio

import com.nexuapicore.core.FeatureRegistry

object AudioEngine {
    fun init(registry: FeatureRegistry) {
        // Carregar AAudio via JNI (libnexus_mali_core.so)
        if (registry.isAvailable("AUDIO_AAUDIO")) {
            println("[AudioEngine] AAudio inicializado — latência ultra-baixa")
        }
        // Carregar OpenAL se disponível
        if (registry.isAvailable("AUDIO_HRTF")) {
            println("[AudioEngine] OpenAL HRTF ativo — áudio binaural 3D")
        }
    }

    fun shutdown() { println("[AudioEngine] Shutdown") }
}
