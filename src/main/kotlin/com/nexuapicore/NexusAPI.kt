package com.nexuapicore

import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.RenderPipeline
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

        // 1. Carregar base de extensões
        val db = ExtensionDatabase.fromJson("extensions.json")

        // 2. Resolver capabilities com suporte real da GPU (simulado no desktop; real via JNI)
        val resolver = CapabilityResolver(db, emptyList()) // placeholder
        featureRegistry = resolver.resolve()

        // 3. Inicializar gestor de recursos
        resourceManager = ResourceManager()

        // 4. Registar módulos pendentes (que foram registados antes de init)
        pendingModules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(pipeline)
        }
        pendingModules.clear()

        // 5. Carregar módulos automáticos (o ModuleLoader pode varrer a classpath)
        val loader = ModuleLoader()
        val modules = loader.discoverModules()
        modules.forEach { mod ->
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(pipeline)
        }

        // 6. Pipeline pronto
        pipeline = RenderPipeline.assemble(modules)
    }

    fun registerModule(module: NexusModule) {
        if (initialized) {
            // Se a API já foi init, inicializa o módulo imediatamente
            module.onInitialize(featureRegistry)
            module.onRegisterPipeline(pipeline)
        } else {
            // Caso contrário, guarda para depois
            pendingModules.add(module)
            println("[Nexus] Module '${module.id}' registered (pending init)")
        }
    }

    fun startFrame() {
        pipeline.executeFrame()
    }
}
