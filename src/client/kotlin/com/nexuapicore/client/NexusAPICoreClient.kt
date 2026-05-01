package com.nexuapicore.client

import com.nexuapicore.core.nativelink.NexusNativeLoader
import com.nexuapicore.core.fallback.ALLExtensionDetector
import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.ResourceManager
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class NexusAPICoreClient : ClientModInitializer {

    companion object {
        var extensionsDetected = false
        lateinit var featureRegistry: FeatureRegistry
    }

    override fun onInitializeClient() {
        println("[Nexus] Starting client initialization...")

        // Carregar a lib nativa agora (não precisa de contexto GL)
        try {
            NexusNativeLoader.load()
            if (NexusNativeLoader.loaded) {
                NexusNativeLoader.initNexusCore()
                println("[Nexus] Nexus Mali Core pronto.")
            }
        } catch (e: Exception) {
            println("[Nexus] Lib nativa falhou no load: ${e.message}")
        }

        // Preparar pipeline vazia por agora
        val modules = ModuleLoader.discoverModules()
        featureRegistry = FeatureRegistry(emptyMap())
        ResourceManager.init()
        RenderPipeline.assemble(modules)

        // Adiar a deteção de extensões para o primeiro tick (GL já inicializado)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!extensionsDetected) {
                extensionsDetected = true
                detectAndLogExtensions(modules)
            }
        }

        println("[Nexus] Client initialization complete. Extensões serão detetadas no primeiro tick.")
    }

    private fun detectAndLogExtensions(modules: List<com.nexuapicore.core.module.NexusModule>) {
        val allKnown = ExtensionDatabase.getAllExtensions()

        // === FONTE 1: Biblioteca Nativa ===
        var nativeExtensions: List<String> = emptyList()
        try {
            if (NexusNativeLoader.loaded) {
                val raw = NexusNativeLoader.getGLExtensions() ?: ""
                if (raw.isNotEmpty()) {
                    nativeExtensions = raw.split(" ").filter { it.isNotEmpty() }
                }
            }
        } catch (e: Exception) {
            println("[Nexus] Erro ao obter extensões nativas: ${e.message}")
        }

        // === FONTE 2: Renderizador (fallback) ===
        var rendererExtensions: List<String> = emptyList()
        try {
            rendererExtensions = ALLExtensionDetector.detectExtensions()
        } catch (e: Exception) {
            println("[Nexus] Erro no fallback GL: ${e.message}")
        }

        // === LOG: Lista Nativa ===
        println("[Nexus] ===== LISTA NATIVA (fonte: libnexus_mali_core.so) =====")
        if (nativeExtensions.isEmpty()) {
            println("[Nexus] [NATIVA] INDISPONÍVEL")
        } else {
            for (def in allKnown) {
                val status = if (nativeExtensions.contains(def.name)) "OK" else "ERRO"
                println("[Nexus] [NAT][$status] ${def.name}")
            }
        }
        println("[Nexus] ===== FIM LISTA NATIVA (${nativeExtensions.size} extensões) =====")

        // === LOG: Lista Renderizador ===
        println("[Nexus] ===== LISTA RENDERIZADOR (fonte: GL/LTW) =====")
        if (rendererExtensions.isEmpty()) {
            println("[Nexus] [RENDERER] INDISPONÍVEL")
        } else {
            for (def in allKnown) {
                val status = if (rendererExtensions.contains(def.name)) "OK" else "ERRO"
                println("[Nexus] [REN][$status] ${def.name}")
            }
        }
        println("[Nexus] ===== FIM LISTA RENDERIZADOR (${rendererExtensions.size} extensões) =====")

        // === Escolher melhor fonte ===
        val availableExtensions = when {
            nativeExtensions.isNotEmpty() -> {
                println("[Nexus] Fonte ativa: Biblioteca Nativa")
                nativeExtensions
            }
            rendererExtensions.isNotEmpty() -> {
                println("[Nexus] Fonte ativa: Renderizador (fallback)")
                rendererExtensions
            }
            else -> {
                println("[Nexus] AVISO: Nenhuma extensão detetada. Modo vanilla ativo.")
                emptyList()
            }
        }

        // Atualizar o registry com as capacidades reais
        val resolver = CapabilityResolver(availableExtensions)
        val capMap = resolver.resolve()
        featureRegistry = FeatureRegistry(capMap)

        modules.forEach { it.onInitialize(featureRegistry) }

        println("[Nexus] Capacidades ativas: ${featureRegistry.getActiveCapabilities()}")
    }
}
