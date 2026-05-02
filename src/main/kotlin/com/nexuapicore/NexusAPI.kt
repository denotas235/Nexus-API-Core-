package com.nexuapicore

import com.nexuapicore.core.ExtensionDatabase
import com.nexuapicore.core.CapabilityResolver
import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.ModuleLoader
import com.nexuapicore.core.ResourceManager
import com.nexuapicore.core.fallback.ALLExtensionDetector
import com.nexuapicore.core.nativelink.NexusNativeLoader
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

        // ── 1. Recolha de extensões com crash safety ──────────────────────
        val availableExtensions: List<String> = collectExtensions()

        println("[Nexus] Extensions detected: ${availableExtensions.size}")

        // ── 2. Resolver capabilities ──────────────────────────────────────
        val capMap = CapabilityResolver(availableExtensions).resolve()
        featureRegistry = FeatureRegistry(capMap)

        val active = featureRegistry.getActiveCapabilities()
        println("[Nexus] Active capabilities (${active.size}): $active")

        // ── 3. Recursos ───────────────────────────────────────────────────
        ResourceManager.init()

        // ── 4. Módulos pendentes com crash safety por módulo ──────────────
        val toInit = pendingModules.toList()
        pendingModules.clear()
        for (mod in toInit) {
            initModuleSafe(mod)
        }

        // ── 5. Módulos descobertos com crash safety por módulo ────────────
        val discovered = try {
            ModuleLoader.discoverModules()
        } catch (e: Throwable) {
            println("[Nexus] ModuleLoader falhou: ${e.message} — continuando sem módulos descobertos")
            emptyList()
        }
        for (mod in discovered) {
            initModuleSafe(mod)
        }

        RenderPipeline.assemble(discovered)
        println("[Nexus] API initialized")
    }

    // ── Recolha de extensões: nativa + GL fallback ────────────────────────
    private fun collectExtensions(): List<String> {
        val combined = mutableSetOf<String>()

        // Tenta carregar biblioteca nativa
        val nativeLoaded = try {
            NexusNativeLoader.load()
            NexusNativeLoader.loaded
        } catch (e: Throwable) {
            println("[Nexus] NexusNativeLoader.load() crash: ${e.message}")
            false
        }

        if (nativeLoaded) {
            println("[Nexus] Fonte ativa: Biblioteca Nativa")

            // GL: GL_ARM_*, GL_EXT_*, GL_OES_*, GL_KHR_*
            safeNativeCall("getGLExtensions") {
                NexusNativeLoader.getGLExtensions()
            }?.split(" ")?.filter { it.isNotBlank() }?.also {
                println("[Nexus] GL extensions (nativo): ${it.size}")
                combined.addAll(it)
            }

            // EGL: EGL_ANDROID_*, EGL_ARM_*, EGL_KHR_*, EGL_EXT_*
            safeNativeCall("getEGLExtensions") {
                NexusNativeLoader.getEGLExtensions()
            }?.split(" ")?.filter { it.isNotBlank() }?.also {
                println("[Nexus] EGL extensions (nativo): ${it.size}")
                combined.addAll(it)
            }

            // Audio: ANDROID_AAUDIO, ANDROID_OPENSL_ES, ALC_*
            safeNativeCall("getAudioExtensions") {
                NexusNativeLoader.getAudioExtensions()
            }?.split(" ")?.filter { it.isNotBlank() }?.also {
                println("[Nexus] Audio extensions (nativo): ${it.size}")
                combined.addAll(it)
            }

            // Vulkan: só regista disponibilidade, não dá extensões reais
            safeNativeCall("getVulkanExtensions") {
                NexusNativeLoader.getVulkanExtensions()
            }?.also { println("[Nexus] Vulkan: $it") }
        }

        // Fallback GL sempre corre — cobre extensões que a .so possa não retornar
        // e é o único caminho se a nativa não carregou
        val glFallback = try {
            ALLExtensionDetector.detectExtensions()
        } catch (e: Throwable) {
            println("[Nexus] ALLExtensionDetector crash: ${e.message} — sem extensões GL")
            emptyList()
        }
        combined.addAll(glFallback)

        if (!nativeLoaded) {
            println("[Nexus] Fonte ativa: Fallback GL")
        }

        println("[Nexus] Extensões combinadas total: ${combined.size}")
        return combined.toList()
    }

    // ── Chama método nativo sem crashar a JVM ─────────────────────────────
    private fun safeNativeCall(name: String, block: () -> String?): String? {
        return try {
            block()
        } catch (e: Throwable) {
            println("[Nexus] Native call '$name' crash: ${e.message} — ignorado")
            null
        }
    }

    // ── Inicializa módulo sem crashar o Minecraft ─────────────────────────
    private fun initModuleSafe(mod: NexusModule) {
        try {
            mod.onInitialize(featureRegistry)
            mod.onRegisterPipeline(RenderPipeline)
            println("[Nexus] Módulo '${mod.id}' inicializado")
        } catch (e: Throwable) {
            println("[Nexus] ERRO no módulo '${mod.id}': ${e.message} — módulo desativado, Minecraft continua")
        }
    }

    fun registerModule(module: NexusModule) {
        if (initialized) {
            initModuleSafe(module)
        } else {
            pendingModules.add(module)
            println("[Nexus] Module '${module.id}' registered (pending init)")
        }
    }

    fun startFrame() {
        try {
            RenderPipeline.executeFrame()
        } catch (e: Throwable) {
            println("[Nexus] RenderPipeline.executeFrame() crash: ${e.message} — frame ignorado")
        }
    }
}
