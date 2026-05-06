package com.nexuapicore

import java.util.function.Consumer

import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline

object NexusAPI {

    private val onReadyCallbacks = mutableListOf<Consumer<FeatureRegistry>>()

    @JvmStatic fun isReady(): Boolean = featureRegistry != null

    @JvmStatic
    fun getRegistry(): FeatureRegistry =
        featureRegistry ?: throw IllegalStateException("Nexus API Core ainda não está pronto. Use NexusAPI.onReady() para aguardar.")

    @JvmStatic
    fun onReady(callback: Consumer<FeatureRegistry>) {
        val reg = featureRegistry
        if (reg != null) {
            callback.accept(reg)
        } else {
            synchronized(onReadyCallbacks) {
                if (featureRegistry != null) {
                    callback.accept(featureRegistry!!)
                } else {
                    onReadyCallbacks.add(callback)
                }
            }
        }
    }

    private fun notifyReady(registry: FeatureRegistry) {
        synchronized(onReadyCallbacks) {
            onReadyCallbacks.forEach { it.accept(registry) }
            onReadyCallbacks.clear()
        }
    }

    @JvmStatic
    @Volatile var featureRegistry: FeatureRegistry? = null

    lateinit var pipeline: RenderPipeline
        private set

    private val pendingModules = mutableListOf<NexusModule>()
    var initialized = false
        private set

    /** Inicializa o registry com as extensões detetadas (ex: do Detector Infalível). */
    fun init(detectedExtensions: List<String>) {
        if (initialized) return
        initialized = true

        println("[Nexus] init() chamado com ${detectedExtensions.size} extensões detetadas.")

        val allExtensions = ExtensionDatabase.getAllExtensions()
        println("[Nexus] Extension database loaded: ${allExtensions.size} known extensions")

        // Passa as extensões detetadas (não vazias!) para o CapabilityResolver
        val resolver = CapabilityResolver(detectedExtensions)
        val capMap = resolver.resolve()
        val registry = FeatureRegistry(capMap)
        featureRegistry = registry
        notifyReady(registry)

        pendingModules.forEach { mod ->
            mod.onInitialize(registry)
            mod.onRegisterPipeline(RenderPipeline)
        }
        pendingModules.clear()

        val modules = ModuleLoader.discoverModules()
        modules.forEach { mod ->
            mod.onInitialize(registry)
            mod.onRegisterPipeline(RenderPipeline)
        }
        RenderPipeline.assemble(modules)
        pipeline = RenderPipeline

        println("[Nexus] API initialized with ${capMap.size} capabilities")
    }

    @JvmStatic fun registerModule(module: NexusModule) {
        if (initialized) {
            module.onInitialize(featureRegistry!!)
            module.onRegisterPipeline(pipeline)
        } else {
            pendingModules.add(module)
            println("[Nexus] Module '${module.id}' registered (pending init)")
        }
    }

    fun startFrame() { pipeline.executeFrame() }
}
