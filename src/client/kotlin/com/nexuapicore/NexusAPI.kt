package com.nexuapicore

import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline

object NexusAPI {
    @JvmStatic
    lateinit var featureRegistry: FeatureRegistry
        private set

    lateinit var pipeline: RenderPipeline
        private set

    private val pendingModules = mutableListOf<NexusModule>()
    var initialized = false
        private set

    fun init() {
        if (initialized) return
        initialized = true

        val allExtensions = ExtensionDatabase.getAllExtensions()
        println("[Nexus] Extension database loaded: ${allExtensions.size} known extensions")

        val resolver = CapabilityResolver(emptyList())
        val capMap = resolver.resolve()
        featureRegistry = FeatureRegistry(capMap)

        pendingModules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }
        pendingModules.clear()

        val modules = ModuleLoader.discoverModules()
        modules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }
        RenderPipeline.assemble(modules)
        pipeline = RenderPipeline

        println("[Nexus] API initialized")
    }

    fun registerModule(module: NexusModule) {
        if (initialized) {
            module.onInitialize(featureRegistry)
            module.onRegisterPipeline(pipeline)
        } else {
            pendingModules.add(module)
            println("[Nexus] Module '${module.id}' registered (pending init)")
        }
    }

    fun startFrame() { pipeline.executeFrame() }
}
