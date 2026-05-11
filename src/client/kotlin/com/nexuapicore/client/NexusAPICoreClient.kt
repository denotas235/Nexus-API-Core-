package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import com.nexuapicore.core.fallback.ALLExtensionDetector
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.loader.api.FabricLoader

class NexusAPICoreClient : ClientModInitializer {

    private val hasMaliOpt = FabricLoader.getInstance().isModLoaded("nexus-maliopt")

    override fun onInitializeClient() {

        ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
            println("[Nexus] GL context ready — executando Detetor Infalivel 2.0")
            val detected = ALLExtensionDetector.detectExtensions()
            println("[Nexus] Detetor concluido — ${detected.size} extensoes encontradas.")
            NexusAPI.init(detected)
            // ShadowPass.init() e invocado pelo GameRendererMixin no primeiro frame GL
        }

        // Shadow map depth pass — ANTES das entidades (apenas se nexus-maliopt instalado)
        if (hasMaliOpt) {
            WorldRenderEvents.BEFORE_ENTITIES.register {
                try {
                    val shadowPass = Class.forName("com.maliopt.pipeline.ShadowPass")
                    val isReady = shadowPass.getMethod("isReady").invoke(null) as Boolean
                    if (isReady) {
                        val mc = net.minecraft.client.MinecraftClient.getInstance()
                        if (mc != null && mc.world != null) {
                            shadowPass.getMethod("render", net.minecraft.client.MinecraftClient::class.java)
                                .invoke(null, mc)
                        }
                    }
                } catch (t: Throwable) {
                    println("[Nexus] ShadowPass.render erro (silenciado): ${t.message}")
                }
            }
        }

        // Post-process pipeline — APOS tudo renderizado
        WorldRenderEvents.END.register {
            try {
                RenderPipeline.executeFrame()
            } catch (e: Throwable) {
                println("[Nexus] RenderPipeline crash: ${e.message} — frame ignorado")
            }
        }
    }
}

