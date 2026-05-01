package com.nexuapicore.core.module

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.pipeline.RenderPipeline

interface NexusModule {
    val id: String
    val requiredCapabilities: Set<String>
    val optionalCapabilities: Set<String>
    fun onInitialize(registry: FeatureRegistry)
    fun onRegisterPipeline(pipeline: RenderPipeline)
    fun onShutdown()
}
