package com.nexuapicore

import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.ResourceManager
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

        // 1. Carregar base de extensões (já está em ExtensionDatabase)
        val allExtensions = ExtensionDatabase.getAllExtensions()
        println("[Nexus] Extension database loaded: ${allExtensions.size} known extensions")

        // 2. Obter lista de extensões disponíveis (será preenchida pelo cliente após o primeiro tick)
        //    Por enquanto, usamos uma lista vazia; o cliente atualizará o registry mais tarde.
        val availableExtensions = emptyList<String>()
        val resolver = CapabilityResolver(availableExtensions)
        val capMap = resolver.resolve()
        featureRegistry = FeatureRegistry(capMap)

        // 3. Inicializar gestor de recursos
        resourceManager = ResourceManager
        ResourceManager.init()   // object, apenas chama o método

        // 4. Registar módulos pendentes
        pendingModules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }
        pendingModules.clear()

        // 5. Carregar módulos automáticos (se houver outros descobertos)
        val loader = ModuleLoader
        val modules = loader.discoverModules()
        modules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
        }

        // 6. Pipeline pronto
        RenderPipeline.assemble(modules)
        pipeline = RenderPipeline   // guardamos o object para facilitar acesso

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
