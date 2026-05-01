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

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("[Nexus] Starting client initialization...")

        // === FONTE 1: Biblioteca Nativa ===
        var nativeExtensions: List<String> = emptyList()
        try {
            NexusNativeLoader.load()
            if (NexusNativeLoader.loaded && NexusNativeLoader.initNexusCore()) {
                val raw = NexusNativeLoader.getGLExtensions() ?: ""
                if (raw.isNotEmpty()) {
                    nativeExtensions = raw.split(" ").filter { it.isNotEmpty() }
                }
            }
        } catch (e: Exception) {
            println("[Nexus] Lib nativa falhou: ${e.message}")
        }

        // === FONTE 2: Renderizador (fallback) ===
        var rendererExtensions: List<String> = emptyList()
        try {
            rendererExtensions = ALLExtensionDetector.detectExtensions()
        } catch (e: Exception) {
            println("[Nexus] Fallback GL falhou: ${e.message}")
        }

        // === LOG: Lista Nativa (173 extensões) ===
        val allKnown = ExtensionDatabase.getAllExtensions()
        println("[Nexus] ===== LISTA NATIVA (fonte: libnexus_mali_core.so) =====")
        if (nativeExtensions.isEmpty()) {
            println("[Nexus] [NATIVA] INDISPONÍVEL — biblioteca não carregada ou sem contexto GL.")
        } else {
            for (def in allKnown) {
                val status = if (nativeExtensions.contains(def.name)) "OK" else "ERRO"
                println("[Nexus] [NAT][$status] ${def.name}")
            }
        }
        println("[Nexus] ===== FIM LISTA NATIVA =====")

        // === LOG: Lista Renderizador (173 extensões) ===
        println("[Nexus] ===== LISTA RENDERIZADOR (fonte: GL/LTW) =====")
        if (rendererExtensions.isEmpty()) {
            println("[Nexus] [RENDERER] INDISPONÍVEL — contexto GL não acessível.")
        } else {
            for (def in allKnown) {
                val status = if (rendererExtensions.contains(def.name)) "OK" else "ERRO"
                println("[Nexus] [REN][$status] ${def.name}")
            }
        }
        println("[Nexus] ===== FIM LISTA RENDERIZADOR =====")

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

        // === Pipeline ===
        val resolver = CapabilityResolver(availableExtensions)
        val capMap = resolver.resolve()
        val featureRegistry = FeatureRegistry(capMap)

        val modules = ModuleLoader.discoverModules()
        modules.forEach {
            it.onInitialize(featureRegistry)
            it.onRegisterPipeline(RenderPipeline)
        }

        ResourceManager.init()
        RenderPipeline.assemble(modules)

        println("[Nexus] Client initialized. Active capabilities: ${featureRegistry.getActiveCapabilities()}")
    }
}
