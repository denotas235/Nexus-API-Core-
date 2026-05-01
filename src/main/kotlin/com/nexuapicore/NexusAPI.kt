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
    lateinit var pipeline: RenderPipeline
        private set
    lateinit var resourceManager: ResourceManager
        private set

    private val pendingModules = mutableListOf<NexusModule>()
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true

        // 1. Base de extensões conhecidas
        val allExtensions = ExtensionDatabase.getAllExtensions()
        println("[Nexus] Extension database loaded: ${allExtensions.size} known extensions")

        // 2. Detectar extensões reais via GL — ALLExtensionDetector chama glGetString(GL_EXTENSIONS)
        //    Só funciona após o contexto GL estar activo (chamado do CLIENT_STARTED)
        val availableExtensions = ALLExtensionDetector.detectExtensions()
        println("[Nexus] Extensions detected: ${availableExtensions.size}")

        // 3. Resolver capabilities
        val resolver = CapabilityResolver(availableExtensions)
        val capMap   = resolver.resolve()
        featureRegistry = FeatureRegistry(capMap)

        val active = featureRegistry.getActiveCapabilities()
        println("[Nexus] Active capabilities (${active.size}): $active")

        // 4. Recursos
        resourceManager = ResourceManager
        ResourceManager.init()

        // 5. Módulos pendentes (registados antes do init)
        pendingModules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }
        pendingModules.clear()

        // 6. Módulos descobertos automaticamente
        val discovered = ModuleLoader.discoverModules()
        discovered.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }

        RenderPipeline.assemble(discovered)
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

    fun startFrame() {
        pipeline.executeFrame()
    }
}
