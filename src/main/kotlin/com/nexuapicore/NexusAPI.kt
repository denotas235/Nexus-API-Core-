package com.nexuapicore

import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.ResourceManager
import com.nexuapicore.core.fallback.ALLExtensionDetector
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline

object NexusAPI {
    lateinit var featureRegistry: FeatureRegistry
        private set

    private val pendingModules = mutableListOf<NexusModule>()
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true

        val allExtensions = ExtensionDatabase.getAllExtensions()
        println("[Nexus] Extension database loaded: ${allExtensions.size} known extensions")

        val availableExtensions = ALLExtensionDetector.detectExtensions()
        println("[Nexus] Extensions detected: ${availableExtensions.size}")

        val resolver = CapabilityResolver(availableExtensions)
        val capMap   = resolver.resolve()
        featureRegistry = FeatureRegistry(capMap)

        val active = featureRegistry.getActiveCapabilities()
        println("[Nexus] Active capabilities (${active.size}): $active")

        ResourceManager.init()

        // Módulos pendentes registados antes do init()
        pendingModules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }
        pendingModules.clear()

        // Módulos descobertos por ServiceLoader
        val discovered = ModuleLoader.discoverModules()
        discovered.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }

        RenderPipeline.assemble(discovered)
        println("[Nexus] API initialized")
    }

    fun registerModule(module: NexusModule) {
        if (initialized) {
            module.onInitialize(featureRegistry)
            module.onRegisterPipeline(RenderPipeline)
        } else {
            pendingModules.add(module)
            println("[Nexus] Module '${module.id}' registered (pending init)")
        }
    }

    fun startFrame() {
        RenderPipeline.executeFrame()
    }
}
