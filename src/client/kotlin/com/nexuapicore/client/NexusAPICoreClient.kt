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

        // 1. Tentar carregar biblioteca nativa
        NexusNativeLoader.load()
        var nativeSuccess = false
        if (NexusNativeLoader.loaded) {
            try {
                nativeSuccess = NexusNativeLoader.initASTC()
                if (nativeSuccess) {
                    println("[Nexus] Native GLAD inicializado com sucesso.")
                } else {
                    println("[Nexus] Native initASTC() retornou false. a iniciar fallback...")
                }
            } catch (e: Exception) {
                println("[Nexus] Exceção ao chamar initASTC(): ${e.message}")
            }
        } else {
            println("[Nexus] Native library não carregada.")
        }

        // 2. Obter lista de extensões disponíveis
        var availableExtensions: List<String> = emptyList()

        if (nativeSuccess) {
            // Placeholder: se a bridge nativa tivesse uma função para devolver as extensões, usaríamos aqui.
            // Como ainda não temos, usamos uma lista vazia mas marcamos que a inicialização nativa foi bem-sucedida.
            // Futuramente: availableExtensions = NexusNativeBridge.getAvailableExtensions()
            println("[Nexus] Native bridge ativo, mas a lista de extensões será obtida via fallback GL.")
            // Vamos obter via GL de qualquer forma para termos a lista real.
            availableExtensions = ALLExtensionDetector.detectExtensions()
        } else {
            // Caminho de fallback: tentar via GL direto
            println("[Nexus] A tentar fallback: ALLExtensionDetector...")
            availableExtensions = ALLExtensionDetector.detectExtensions()
        }

        // 3. Se ainda assim a lista está vazia, último fallback: continuar sem capacidades
        if (availableExtensions.isEmpty()) {
            println("[Nexus] AVISO: Nenhuma extensão GL detetada. O mod continuará sem capacidades avançadas (modo vanilla).")
        }

        // 4. Resolver capacidades (mesmo com lista vazia, o CapabilityResolver funciona)
        val resolver = CapabilityResolver(availableExtensions)
        val capMap = resolver.resolve()
        val featureRegistry = FeatureRegistry(capMap)

        // 5. Carregar módulos (atualmente vazio)
        val modules = ModuleLoader.discoverModules()
        modules.forEach {
            it.onInitialize(featureRegistry)
            it.onRegisterPipeline(RenderPipeline)
        }

        // 6. Inicializar recursos e pipeline
        ResourceManager.init()
        RenderPipeline.assemble(modules)

        println("[Nexus] Client initialized. Active capabilities: ${featureRegistry.getActiveCapabilities()}")
    }
}
